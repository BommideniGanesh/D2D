from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required
import concurrent.futures
import yfinance as yf
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
from datetime import datetime
from app.scrapers import get_finviz_news, get_yahoo_news, get_cnbc_news, get_benzinga_news, get_fool_news

market_bp = Blueprint('market_bp', __name__)

try:
    nltk.download('vader_lexicon', quiet=True)
except Exception as e:
    print(f"NLTK Download Warning: {e}")


@market_bp.route('/news/<ticker>', methods=['GET'])
@jwt_required()
def get_market_news(ticker):
    results = []

    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        future_finviz  = executor.submit(get_finviz_news,   ticker)
        future_yahoo   = executor.submit(get_yahoo_news,    ticker)
        future_cnbc    = executor.submit(get_cnbc_news,     ticker)
        future_benzinga= executor.submit(get_benzinga_news, ticker)
        future_fool    = executor.submit(get_fool_news,     ticker)

        results.append({"source": "Finviz",       "articles": future_finviz.result()})
        results.append({"source": "Yahoo Finance", "articles": future_yahoo.result()})
        results.append({"source": "CNBC",          "articles": future_cnbc.result()})
        results.append({"source": "Benzinga",      "articles": future_benzinga.result()})
        results.append({"source": "Motley Fool",   "articles": future_fool.result()})

    return jsonify(results), 200


# ---------------------------------------------------------------------------
# NEW: Dynamic Dashboard endpoint
# GET /api/market/dashboard/<ticker>
# Returns: quote, sentiment_summary, structured news, 30-day daily OHLCV
# ---------------------------------------------------------------------------
@market_bp.route('/dashboard/<ticker>', methods=['GET'])
@jwt_required()
def get_dashboard(ticker):
    ticker = ticker.upper()

    # ── 1. Scrape news concurrently ─────────────────────────────────────────
    raw_news = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        f_finviz   = executor.submit(get_finviz_news,   ticker)
        f_yahoo    = executor.submit(get_yahoo_news,    ticker)
        f_cnbc     = executor.submit(get_cnbc_news,     ticker)
        f_benzinga = executor.submit(get_benzinga_news, ticker)
        f_fool     = executor.submit(get_fool_news,     ticker)

        raw_news.extend(f_finviz.result())
        raw_news.extend(f_yahoo.result())
        raw_news.extend(f_cnbc.result())
        raw_news.extend(f_benzinga.result())
        raw_news.extend(f_fool.result())

    # ── 2. yFinance — live quote + 30-day daily history ────────────────────
    stock = yf.Ticker(ticker)

    quote = {'open': 0.0, 'high': 0.0, 'low': 0.0, 'close': 0.0,
             'volume': 0, 'prev_close': 0.0, 'change': 0.0, 'change_pct': 0.0}
    try:
        hist_1d = stock.history(period="2d")
        if len(hist_1d) >= 2:
            prev  = hist_1d.iloc[-2]
            today = hist_1d.iloc[-1]
            change     = round(float(today['Close']) - float(prev['Close']), 4)
            change_pct = round((change / float(prev['Close'])) * 100, 2) if prev['Close'] else 0.0
            quote = {
                'open':       round(float(today['Open']),   2),
                'high':       round(float(today['High']),   2),
                'low':        round(float(today['Low']),    2),
                'close':      round(float(today['Close']),  2),
                'volume':     int(today['Volume']),
                'prev_close': round(float(prev['Close']),   2),
                'change':     change,
                'change_pct': change_pct,
            }
        elif len(hist_1d) == 1:
            today = hist_1d.iloc[-1]
            quote = {
                'open':   round(float(today['Open']),  2),
                'high':   round(float(today['High']),  2),
                'low':    round(float(today['Low']),   2),
                'close':  round(float(today['Close']), 2),
                'volume': int(today['Volume']),
                'prev_close': 0.0, 'change': 0.0, 'change_pct': 0.0,
            }
    except Exception as e:
        print(f"yFinance quote error: {e}")

    # 30-day daily OHLCV
    historical = []
    try:
        hist_30 = stock.history(period="30d", interval="1d")
        for ts, row in hist_30.iterrows():
            o     = round(float(row['Open']),  2)
            h     = round(float(row['High']),  2)
            low_p = round(float(row['Low']),   2)
            c     = round(float(row['Close']), 2)
            historical.append({
                'date':         ts.strftime('%Y-%m-%d'),
                'open':         o,
                'high':         h,
                'low':          low_p,
                'close':        c,
                'volume':       int(row['Volume']),
                'candle_color': 'Green' if c >= o else 'Red',
                'change_pct':   round(((c - o) / o) * 100, 2) if o else 0.0,
            })
        historical.reverse()   # newest first
    except Exception as e:
        print(f"yFinance history error: {e}")

    # ── 3. NLTK sentiment on scraped news ──────────────────────────────────
    sia = SentimentIntensityAnalyzer()
    today_str = datetime.now().strftime('%Y-%m-%d')
    structured = []
    counts = {'Positive': 0, 'Negative': 0, 'Neutral': 0}
    total_confidence = 0.0

    for row in raw_news:
        if not row.get('headline'):
            continue
        scores     = sia.polarity_scores(row['headline'])
        compound   = scores['compound']
        confidence = round(abs(compound), 4)
        sentiment  = 'Positive' if compound > 0.05 else ('Negative' if compound < -0.05 else 'Neutral')

        counts[sentiment] += 1
        total_confidence  += confidence

        structured.append({
            'date':       today_str,
            'ticker':     ticker,
            'headline':   row['headline'],
            'source':     row.get('source', ''),
            'url':        row.get('url', ''),
            'metadata':   row.get('metadata', ''),
            'open':       quote['open'],
            'close':      quote['close'],
            'volume':     quote['volume'],
            'sentiment':  sentiment,
            'confidence': confidence,
            'compound':   round(compound, 4),
        })

    # Sort by confidence descending
    structured.sort(key=lambda x: x['confidence'], reverse=True)

    total_articles = len(structured)
    avg_confidence = round(total_confidence / total_articles, 4) if total_articles else 0.0

    sentiment_summary = {
        'positive_count':  counts['Positive'],
        'negative_count':  counts['Negative'],
        'neutral_count':   counts['Neutral'],
        'total':           total_articles,
        'avg_confidence':  avg_confidence,
        'positive_pct':    round((counts['Positive'] / total_articles) * 100, 1) if total_articles else 0,
        'negative_pct':    round((counts['Negative'] / total_articles) * 100, 1) if total_articles else 0,
        'neutral_pct':     round((counts['Neutral']  / total_articles) * 100, 1) if total_articles else 0,
        'overall':         'Positive' if counts['Positive'] > counts['Negative'] else (
                           'Negative' if counts['Negative'] > counts['Positive'] else 'Neutral'),
    }

    return jsonify({
        'ticker':            ticker,
        'generated_at':      datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ'),
        'quote':             quote,
        'sentiment_summary': sentiment_summary,
        'structured':        structured,
        'historical':        historical,
    }), 200
