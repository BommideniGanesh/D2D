from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required
import concurrent.futures
import yfinance as yf
import nltk
from nltk.sentiment.vader import SentimentIntensityAnalyzer
from datetime import datetime
from app.scrapers import get_finviz_news, get_yahoo_news, get_cnbc_news, get_benzinga_news, get_fool_news

sentiment_bp = Blueprint('sentiment_bp', __name__)

# Pre-load NLTK corpus silently during spinup
try:
    nltk.download('vader_lexicon', quiet=True)
except Exception as e:
    print(f"NLTK Download Warning: {e}")

@sentiment_bp.route('/<ticker>', methods=['GET'])
@jwt_required()
def get_sentiment(ticker):
    raw_news = []
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        f_finviz = executor.submit(get_finviz_news, ticker)
        f_yahoo = executor.submit(get_yahoo_news, ticker)
        f_cnbc = executor.submit(get_cnbc_news, ticker)
        f_benzinga = executor.submit(get_benzinga_news, ticker)
        f_fool = executor.submit(get_fool_news, ticker)
        
        raw_news.extend(f_finviz.result())
        raw_news.extend(f_yahoo.result())
        raw_news.extend(f_cnbc.result())
        raw_news.extend(f_benzinga.result())
        raw_news.extend(f_fool.result())

    stock = yf.Ticker(ticker)
    try:
        hist = stock.history(period="1d")
        if not hist.empty:
            m_data = {
                'Open': round(hist.iloc[-1]['Open'], 2),
                'Close': round(hist.iloc[-1]['Close'], 2),
                'Volume': int(hist.iloc[-1]['Volume'])
            }
        else:
            m_data = {'Open': 0.0, 'Close': 0.0, 'Volume': 0}
    except:
        m_data = {'Open': 0.0, 'Close': 0.0, 'Volume': 0}

    structured = []
    local_sia = SentimentIntensityAnalyzer()
    
    for row in raw_news:
        if not row.get('headline'): continue
        score = local_sia.polarity_scores(row['headline'])
        sentiment = "Positive" if score['compound'] > 0.05 else "Negative" if score['compound'] < -0.05 else "Neutral"
        confidence = abs(score['compound'])
        
        structured.append({
            'date': datetime.now().strftime('%Y-%m-%d'),
            'ticker': ticker.upper(),
            'headline': row['headline'],
            'source': row['source'],
            'open': m_data['Open'],
            'close': m_data['Close'],
            'volume': m_data['Volume'],
            'sentiment': sentiment,
            'confidence': round(confidence, 2),
            'url': row.get('url', ''),
            'metadata': row.get('metadata', '')
        })

    return jsonify({
        "raw": raw_news,
        "structured": structured
    }), 200
