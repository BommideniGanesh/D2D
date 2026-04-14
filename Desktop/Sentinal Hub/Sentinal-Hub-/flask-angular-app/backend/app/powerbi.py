"""
Power BI Data Endpoints
========================
Two public (no-JWT) endpoints that return flat JSON records
Power BI can consume directly via the Web connector.

Endpoints:
  GET /api/powerbi/news-features/<ticker>
  GET /api/powerbi/historical/<ticker>?days=30
"""

from flask import Blueprint, jsonify, request
from datetime import datetime
import yfinance as yf
from nltk.sentiment.vader import SentimentIntensityAnalyzer

powerbi_bp = Blueprint("powerbi_bp", __name__)


# ---------------------------------------------------------------------------
# 1. NEWS-BASED SENTIMENT FEATURES  →  Power BI
#    Source: yFinance news (no MT5, no scraper, no timeout)
# ---------------------------------------------------------------------------
@powerbi_bp.route("/news-features/<ticker>", methods=["GET"])
def news_features(ticker):
    """
    Returns one flat JSON row per news headline with VADER sentiment scores.

    Fields:
      ticker, headline, source, published_at, scraped_at,
      compound, positive, negative, neutral,
      sentiment_label, sentiment_numeric
    """
    ticker  = ticker.upper()
    now_str = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")

    try:
        stock      = yf.Ticker(ticker)
        news_items = stock.news or []
    except Exception as e:
        return jsonify({"error": f"yFinance news fetch failed: {str(e)}"}), 500

    sia  = SentimentIntensityAnalyzer()
    rows = []

    for item in news_items:
        # Support both old and new yFinance news schema
        content  = item.get("content", {})
        headline = content.get("title", "") or item.get("title", "")
        if not headline:
            continue

        scores   = sia.polarity_scores(headline)
        compound = round(scores["compound"], 4)
        pos      = round(scores["pos"],      4)
        neg      = round(scores["neg"],      4)
        neu      = round(scores["neu"],      4)

        if compound >= 0.05:
            label, numeric = "Positive",  1
        elif compound <= -0.05:
            label, numeric = "Negative", -1
        else:
            label, numeric = "Neutral",   0

        pub_ts = (
            content.get("pubDate", "")
            or item.get("providerPublishTime", "")
        )
        if isinstance(pub_ts, (int, float)):
            pub_ts = datetime.utcfromtimestamp(pub_ts).strftime("%Y-%m-%dT%H:%M:%SZ")

        source = (
            content.get("provider", {}).get("displayName", "")
            or item.get("publisher", "")
        )

        rows.append({
            "ticker":            ticker,
            "headline":          headline,
            "source":            source,
            "published_at":      pub_ts or now_str,
            "scraped_at":        now_str,
            "compound":          compound,
            "positive":          pos,
            "negative":          neg,
            "neutral":           neu,
            "sentiment_label":   label,
            "sentiment_numeric": numeric,
        })

    return jsonify({
        "ticker":       ticker,
        "record_count": len(rows),
        "generated_at": now_str,
        "data":         rows,
    }), 200


# ---------------------------------------------------------------------------
# 2. HISTORICAL PRICE DATA (yFinance OHLCV)  →  Power BI
#    No MT5 required — works immediately in Power BI Web connector
# ---------------------------------------------------------------------------
@powerbi_bp.route("/historical/<ticker>", methods=["GET"])
def historical_data(ticker):
    """
    Returns flat OHLCV data from yFinance.

    Query params:
      days      (default 30)  — look-back window in calendar days
      interval  (default 1d)  — 5m, 15m, 1h, 1d

    Fields:
      ticker, date, open, high, low, close,
      volume, candle_color, body_size, upper_wick, lower_wick, change_pct
    """
    ticker   = ticker.upper()
    days     = int(request.args.get("days", 30))
    interval = request.args.get("interval", "1d")

    try:
        stock = yf.Ticker(ticker)
        hist  = stock.history(period=f"{days}d", interval=interval)
    except Exception as e:
        return jsonify({"error": f"yFinance history fetch failed: {str(e)}"}), 500

    if hist is None or hist.empty:
        return jsonify({"error": f"No price data found for {ticker}."}), 404

    rows = []
    for ts, row in hist.iterrows():
        o   = round(float(row["Open"]),  5)
        h   = round(float(row["High"]),  5)
        lo  = round(float(row["Low"]),   5)
        c   = round(float(row["Close"]), 5)
        vol = int(row["Volume"])

        rows.append({
            "ticker":       ticker,
            "date":         ts.strftime("%Y-%m-%d %H:%M:%S"),
            "open":         o,
            "high":         h,
            "low":          lo,
            "close":        c,
            "volume":       vol,
            "candle_color": "Green" if c >= o else "Red",
            "body_size":    round(abs(c - o),        5),
            "upper_wick":   round(h - max(o, c),     5),
            "lower_wick":   round(min(o, c) - lo,    5),
            "change_pct":   round(((c - o) / o) * 100, 2) if o else 0.0,
        })

    # Newest first
    rows.sort(key=lambda x: x["date"], reverse=True)

    now_str = datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
    return jsonify({
        "ticker":       ticker,
        "days":         days,
        "interval":     interval,
        "record_count": len(rows),
        "generated_at": now_str,
        "data":         rows,
    }), 200
