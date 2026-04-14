from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
import MetaTrader5 as mt5
import pandas as pd
from sklearn.linear_model import LinearRegression
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
import concurrent.futures
from app.scrapers import get_finviz_news, get_yahoo_news
from app.models import Mt5Credential, User
from app import db

trading_bp = Blueprint("trading_bp", __name__)


@trading_bp.route("/credentials", methods=["GET", "POST"])
@jwt_required()
def mt5_credentials():
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user:
        return jsonify({"message": "User not found"}), 404

    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    if request.method == "GET":
        if cred:
            return jsonify(cred.to_dict()), 200
        else:
            return jsonify({"message": "No credentials found"}), 404

    # POST Configuration
    data = request.json
    login_id = data.get("login_id")
    password = data.get("password")
    server = data.get("server")

    if not all([login_id, password, server]):
        return jsonify({"message": "Missing required MT5 configuration fields."}), 400

    if cred:
        cred.login_id = int(login_id)
        cred.password = password
        cred.server = server
    else:
        cred = Mt5Credential(
            user_id=user_id, login_id=int(login_id), password=password, server=server
        )
        db.session.add(cred)

    db.session.commit()
    return jsonify({"message": "MT5 credentials saved successfully!"}), 200


@trading_bp.route("/connect", methods=["GET"])
@jwt_required()
def connect_mt5():
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    if not cred:
        return jsonify(
            {"message": "No MT5 credentials configured. Please navigate to settings."}
        ), 400

    # Initialize the MT5 terminal
    if not mt5.initialize():
        return jsonify(
            {
                "message": f"MT5 initialization failed! Make sure the MT5 Desktop Terminal is currently OPEN! Error Code: {mt5.last_error()}"
            }
        ), 500

    # Attempt to login using database securely pulled constraints
    authorized = mt5.login(
        login=int(cred.login_id), password=cred.password, server=cred.server
    )

    if authorized:
        acc_info = mt5.account_info()
        if acc_info is not None:
            data = {
                "Broker": acc_info.company,
                "Balance": acc_info.balance,
                "Equity": acc_info.equity,
                "Currency": acc_info.currency,
                "Profit": acc_info.profit,
            }
            return jsonify({"message": "Connected", "data": data}), 200
        else:
            return jsonify(
                {
                    "message": "Login successful, but failed to fetch account info from broker."
                }
            ), 500
    else:
        err = mt5.last_error()
        mt5.shutdown()
        return jsonify(
            {
                "message": f"Failed to connect to account #{cred.login_id}. Check login credentials. Error: {err}"
            }
        ), 401


@trading_bp.route("/history", methods=["GET"])
@jwt_required()
def history_mt5():
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    if not cred:
        return jsonify({"message": "No MT5 credentials configured."}), 400

    if not mt5.initialize():
        return jsonify(
            {"message": f"MT5 initialization failed! Error: {mt5.last_error()}"}
        ), 500

    authorized = mt5.login(
        login=int(cred.login_id), password=cred.password, server=cred.server
    )

    if not authorized:
        mt5.shutdown()
        return jsonify({"message": "Failed to connect to MT5 account."}), 401

    from datetime import datetime, timezone, timedelta

    utc_from = datetime(2000, 1, 1, tzinfo=timezone.utc)
    # Add a generous buffer so broker time ahead of UTC is still captured
    utc_to = datetime.now(timezone.utc) + timedelta(hours=24)

    deals = mt5.history_deals_get(utc_from, utc_to)

    if deals is None:
        mt5.shutdown()
        return jsonify(
            {"message": f"Failed to get deals. Error: {mt5.last_error()}"}
        ), 500

    history = []
    for deal in deals:
        history.append(
            {
                "ticket": deal.ticket,
                "time": int(deal.time)
                - 10800,  # Broker server is UTC+3; subtract 3h to get UTC for CDT display
                "type": deal.type,
                "entry": deal.entry,
                "volume": deal.volume,
                "price": deal.price,
                "profit": deal.profit
                + (getattr(deal, "commission", 0.0) or 0.0)
                + (getattr(deal, "swap", 0.0) or 0.0)
                + (getattr(deal, "fee", 0.0) or 0.0),
                "symbol": deal.symbol,
                "comment": deal.comment,
            }
        )

    history.sort(key=lambda x: x["time"], reverse=True)

    return jsonify({"message": "Success", "data": history}), 200


@trading_bp.route("/rates/<ticker>", methods=["GET"])
@jwt_required()
def get_rates(ticker):
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    if not cred:
        return jsonify({"message": "No MT5 credentials configured."}), 400

    if not mt5.initialize():
        return jsonify(
            {"message": f"MT5 initialization failed! Error: {mt5.last_error()}"}
        ), 500

    authorized = mt5.login(
        login=int(cred.login_id), password=cred.password, server=cred.server
    )

    if not authorized:
        mt5.shutdown()
        return jsonify({"message": "Failed to connect to MT5 account."}), 401

    from datetime import datetime, timezone, timedelta

    days_back = 7
    # Use UTC-aware datetimes so MT5 queries the correct range
    utc_from = datetime.now(timezone.utc) - timedelta(days=days_back)
    utc_to = datetime.now(timezone.utc)

    rates = mt5.copy_rates_range(ticker, mt5.TIMEFRAME_M5, utc_from, utc_to)

    if rates is None or len(rates) == 0:
        mt5.shutdown()
        return jsonify(
            {"message": f"Failed to get rates or no data. Error: {mt5.last_error()}"}
        ), 404

    data = []
    # format rates to match the pandas dataframe logic from the user snippet
    for rate in rates:
        data.append(
            {
                "time": int(rate["time"]) - 10800,  # Broker server is UTC+3; subtract 3h to get real UTC
                "open": float(rate["open"]),
                "high": float(rate["high"]),
                "low": float(rate["low"]),
                "close": float(rate["close"]),
                "tick_volume": int(rate["tick_volume"]),
            }
        )

    # Order descending so the latest candles are first
    data.sort(key=lambda x: x["time"], reverse=True)

    return jsonify({"message": "Success", "data": data, "count": len(data)}), 200


@trading_bp.route("/execute", methods=["POST"])
@jwt_required()
def execute_trade():
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    if not cred:
        return jsonify({"message": "No MT5 credentials configured."}), 400

    if not mt5.initialize():
        return jsonify(
            {"message": f"MT5 initialization failed! Error: {mt5.last_error()}"}
        ), 500

    authorized = mt5.login(
        login=int(cred.login_id), password=cred.password, server=cred.server
    )

    if not authorized:
        mt5.shutdown()
        return jsonify({"message": "Failed to connect to MT5 account."}), 401

    data = request.json
    ticker    = data.get("ticker")
    action    = data.get("action")        # 'BUY' or 'SELL'
    volume    = float(data.get("volume",    1.0))
    rr_ratio  = float(data.get("rr_ratio",  2.0))   # Risk:Reward (e.g. 2.0 → 1:2)
    risk_pips = int(data.get("risk_pips",   100))    # Distance to SL in points

    if not all([ticker, action]):
        return jsonify({"message": "Missing required fields (ticker, action)."}), 400

    action = action.upper()
    if action not in ["BUY", "SELL"]:
        return jsonify({"message": "Invalid action. Use BUY or SELL."}), 400

    # ── Symbol info ────────────────────────────────────────────────────────────
    mt5.symbol_select(ticker, True)
    symbol_info = mt5.symbol_info(ticker)
    if symbol_info is None:
        return jsonify({"message": f"{ticker} not found in MT5."}), 404

    tick = mt5.symbol_info_tick(ticker)
    if tick is None:
        return jsonify({"message": f"Failed to get live tick for {ticker}."}), 400

    digits = symbol_info.digits
    point  = symbol_info.point

    # ── Volume validation (clamp to broker limits) ─────────────────────────────
    import math
    vol_min  = symbol_info.volume_min  if symbol_info.volume_min  > 0 else 1.0
    vol_max  = symbol_info.volume_max  if symbol_info.volume_max  > 0 else 1000.0
    vol_step = symbol_info.volume_step if symbol_info.volume_step > 0 else 1.0

    steps    = round(volume / vol_step)
    volume   = round(steps * vol_step, 10)
    volume   = max(vol_min, min(vol_max, volume))
    dec_pl   = max(0, -int(math.floor(math.log10(vol_step)))) if vol_step < 1 else 0
    volume   = round(volume, dec_pl)

    # ── Entry price ────────────────────────────────────────────────────────────
    price      = tick.ask if action == "BUY" else tick.bid
    order_type = mt5.ORDER_TYPE_BUY if action == "BUY" else mt5.ORDER_TYPE_SELL

    # ── SL / TP — calculated server-side from action direction ─────────────────
    # This prevents the 400 error caused by inverted SL/TP from the frontend.
    if action == "BUY":
        sl = round(price - (risk_pips * point), digits)
        tp = round(price + ((price - sl) * rr_ratio), digits)
    else:  # SELL
        sl = round(price + (risk_pips * point), digits)
        tp = round(price - ((sl - price) * rr_ratio), digits)

    # ── Filling mode (broker bitmask) ──────────────────────────────────────────
    try:
        fm = symbol_info.filling_mode
        if fm & 1:   filling = 0   # ORDER_FILLING_FOK
        elif fm & 2: filling = 1   # ORDER_FILLING_IOC
        else:        filling = 2   # ORDER_FILLING_RETURN
    except Exception:
        filling = 2

    # ── Build & send order ─────────────────────────────────────────────────────
    request_payload = {
        "action":       mt5.TRADE_ACTION_DEAL,
        "symbol":       ticker,
        "volume":       volume,
        "type":         order_type,
        "price":        price,
        "sl":           sl,
        "tp":           tp,
        "deviation":    10,
        "magic":        202604,
        "comment":      f"RR 1:{rr_ratio} | {action}",
        "type_time":    mt5.ORDER_TIME_GTC,
        "type_filling": filling,
    }

    result = mt5.order_send(request_payload)

    if result.retcode != mt5.TRADE_RETCODE_DONE:
        return jsonify({
            "message": f"Order failed! {result.comment} (Code: {result.retcode}). "
                       f"Entry: {price:.5f} | SL: {sl:.5f} | TP: {tp:.5f}"
        }), 400

    return jsonify({
        "message": (
            f"✅ {action} {volume} lot(s) of {ticker} @ {price:.5f} | "
            f"SL: {sl:.5f} | TP: {tp:.5f} | RR 1:{rr_ratio} | Ticket #{result.order}"
        )
    }), 200

# ══════════════════════════════════════════════════════════════════
#  OPEN POSITIONS
# ══════════════════════════════════════════════════════════════════
@trading_bp.route("/positions", methods=["GET"])
@jwt_required()
def get_open_positions():
    """Return all currently open positions from the MT5 account."""
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()
    if not cred:
        return jsonify({"message": "No MT5 credentials configured."}), 400

    if not mt5.initialize():
        return jsonify({"message": f"MT5 init failed: {mt5.last_error()}"}), 500

    if not mt5.login(login=int(cred.login_id), password=cred.password, server=cred.server):
        mt5.shutdown()
        return jsonify({"message": "MT5 login failed."}), 401

    positions = mt5.positions_get()
    mt5.shutdown()

    if positions is None:
        return jsonify({"data": [], "count": 0}), 200

    data = []
    for p in positions:
        data.append({
            "ticket":     int(p.ticket),
            "symbol":     p.symbol,
            "type":       int(p.type),         # 0=Buy, 1=Sell
            "type_label": "BUY" if p.type == 0 else "SELL",
            "volume":     float(p.volume),
            "price_open": float(p.price_open),
            "price_current": float(p.price_current),
            "profit":     float(p.profit),
            "sl":         float(p.sl),
            "tp":         float(p.tp),
            "time":       int(p.time),
            "comment":    p.comment,
        })

    # Sort newest first
    data.sort(key=lambda x: x["time"], reverse=True)
    return jsonify({"data": data, "count": len(data)}), 200


# ══════════════════════════════════════════════════════════════════
#  CLOSE POSITION BY TICKET
# ══════════════════════════════════════════════════════════════════
@trading_bp.route("/close", methods=["POST"])
@jwt_required()
def close_position():
    """Close a specific open position by ticket number."""
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()
    if not cred:
        return jsonify({"message": "No MT5 credentials configured."}), 400

    if not mt5.initialize():
        return jsonify({"message": f"MT5 init failed: {mt5.last_error()}"}), 500

    if not mt5.login(login=int(cred.login_id), password=cred.password, server=cred.server):
        mt5.shutdown()
        return jsonify({"message": "MT5 login failed."}), 401

    data   = request.json
    ticket = int(data.get("ticket", 0))

    if ticket == 0:
        mt5.shutdown()
        return jsonify({"message": "Missing ticket number."}), 400

    # Find the live position
    position = mt5.positions_get(ticket=ticket)
    if not position:
        mt5.shutdown()
        return jsonify({"message": f"Position #{ticket} not found or already closed."}), 404

    pos = position[0]
    symbol  = pos.symbol
    volume  = pos.volume
    # Counter order: BUY position → close with SELL and vice-versa
    close_type = mt5.ORDER_TYPE_SELL if pos.type == 0 else mt5.ORDER_TYPE_BUY
    price      = mt5.symbol_info_tick(symbol).bid if pos.type == 0 else mt5.symbol_info_tick(symbol).ask

    # Detect filling mode
    sym_info = mt5.symbol_info(symbol)
    try:
        fm = sym_info.filling_mode
        if fm & 1:   filling = 0
        elif fm & 2: filling = 1
        else:        filling = 2
    except Exception:
        filling = 2

    close_request = {
        "action":       mt5.TRADE_ACTION_DEAL,
        "symbol":       symbol,
        "volume":       volume,
        "type":         close_type,
        "position":     ticket,      # <-- links this order to the open position
        "price":        price,
        "deviation":    20,
        "magic":        202604,
        "comment":      f"Close #{ticket}",
        "type_time":    mt5.ORDER_TIME_GTC,
        "type_filling": filling,
    }

    result = mt5.order_send(close_request)
    mt5.shutdown()

    if result.retcode != mt5.TRADE_RETCODE_DONE:
        return jsonify({
            "message": f"Failed to close #{ticket}: {result.comment} (Code: {result.retcode})"
        }), 400

    return jsonify({
        "message": f"✅ Position #{ticket} ({pos.type_label if hasattr(pos,'type_label') else symbol}) closed @ {price:.5f}. P&L: {pos.profit:+.2f}"
    }), 200


@trading_bp.route("/ml/predict/<ticker>", methods=["GET"])
@jwt_required()
def ml_predict(ticker):
    user_id = get_jwt_identity()
    cred = Mt5Credential.query.filter_by(user_id=user_id).first()

    # 1. Historical Analysis Machine Learning Model (Linear Regression on MT5 data)
    historical_signal = "NEUTRAL"
    historical_confidence = 0.0
    predicted_close = 0.0
    current_price = 0.0

    if cred and mt5.initialize():
        authorized = mt5.login(
            login=int(cred.login_id), password=cred.password, server=cred.server
        )
        if authorized:
            from datetime import datetime, timedelta

            days_back = 7
            utc_from = datetime.now() - timedelta(days=days_back)
            utc_to = datetime.now()
            rates = mt5.copy_rates_range(ticker, mt5.TIMEFRAME_M5, utc_from, utc_to)
            if rates is not None and len(rates) > 10:
                df = pd.DataFrame(rates)
                df["time"] = pd.to_datetime(df["time"], unit="s")
                df["target"] = df["close"].shift(-1)
                df = df.dropna()
                X = df[["open", "high", "low", "close", "tick_volume"]]
                y = df["target"]

                model = LinearRegression()
                model.fit(X, y)

                last_candle = df.iloc[-1][
                    ["open", "high", "low", "close", "tick_volume"]
                ].values.reshape(1, -1)
                pred = model.predict(last_candle)[0]
                predicted_close = round(pred, 5)

                current_price = df.iloc[-1]["close"]
                diff = pred - current_price
                if diff > 0:
                    historical_signal = "BUY"
                elif diff < 0:
                    historical_signal = "SELL"

                historical_confidence = min(
                    round(abs(diff / current_price) * 10000, 2), 100.0
                )

            mt5.shutdown()

    # 2. News Sentiment Machine Learning Model (NLTK VADER)
    raw_news = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        f_finviz = executor.submit(get_finviz_news, ticker)
        f_yahoo = executor.submit(get_yahoo_news, ticker)
        raw_news.extend(f_finviz.result())
        raw_news.extend(f_yahoo.result())

    sia = SentimentIntensityAnalyzer()
    pos_count, neg_count, total = 0, 0, 0
    for row in raw_news:
        if row.get("headline"):
            total += 1
            score = sia.polarity_scores(row["headline"])
            if score["compound"] > 0.05:
                pos_count += 1
            elif score["compound"] < -0.05:
                neg_count += 1

    news_signal = "NEUTRAL"
    news_confidence = 0.0
    if total > 0:
        if pos_count > neg_count:
            news_signal = "BUY"
            news_confidence = round((pos_count / total) * 100, 2)
        elif neg_count > pos_count:
            news_signal = "SELL"
            news_confidence = round((neg_count / total) * 100, 2)

    return jsonify(
        {
            "ticker": ticker.upper(),
            "historical_model": {
                "signal": historical_signal,
                "confidence": historical_confidence,
                "predicted_close": predicted_close,
                "current_price": current_price,
            },
            "news_model": {
                "signal": news_signal,
                "confidence": news_confidence,
                "articles_analyzed": total,
            },
        }
    ), 200
