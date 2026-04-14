# 📈 FinTech AI Trading Platform — Complete Technical Documentation

> A full-stack AI-powered stock trading dashboard built with **Flask (Python)** on the backend and **Angular 19** on the frontend.  
> It integrates MetaTrader 5 for live brokerage operations, scikit-learn for ML price predictions, NLTK VADER for news sentiment analysis, yFinance for market data, and Power BI for analytics exports.

---

## Table of Contents

1. [Project Architecture Overview](#1-project-architecture-overview)
2. [Technology Stack](#2-technology-stack)
3. [Directory Structure](#3-directory-structure)
4. [Backend — Flask Application](#4-backend--flask-application)
   - [App Factory & Startup](#41-app-factory--startup)
   - [Database Models](#42-database-models)
   - [Authentication System (Login & Logout)](#43-authentication-system-login--logout)
   - [KYC (Know Your Customer)](#44-kyc-know-your-customer)
   - [MetaTrader 5 Integration](#45-metatrader-5-integration)
   - [Machine Learning — Linear Regression](#46-machine-learning--linear-regression)
   - [News Sentiment — NLTK VADER](#47-news-sentiment--nltk-vader)
   - [News Scrapers](#48-news-scrapers)
   - [Market Dashboard Endpoint](#49-market-dashboard-endpoint)
   - [Power BI Data Endpoints](#410-power-bi-data-endpoints)
5. [Frontend — Angular 19 Application](#5-frontend--angular-19-application)
   - [App Bootstrap & Configuration](#51-app-bootstrap--configuration)
   - [Routing & Route Guards](#52-routing--route-guards)
   - [Auth Service (Login / Logout Flow)](#53-auth-service-login--logout-flow)
   - [JWT HTTP Interceptor](#54-jwt-http-interceptor)
   - [Feature Services](#55-feature-services)
   - [Pages Overview](#56-pages-overview)
6. [API Reference](#6-api-reference)
7. [Data Flow Diagrams](#7-data-flow-diagrams)
8. [Setup & Running Locally](#8-setup--running-locally)
9. [Security Design](#9-security-design)
10. [Known Limitations & Notes](#10-known-limitations--notes)

---

## 1. Project Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   BROWSER (localhost:4200)                   │
│                   Angular 19 SPA Frontend                   │
│   Login → KYC Guard → Dashboard Pages → ML Trading Bot      │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP REST (JSON)
                       │ Bearer JWT Token on every request
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               Flask 3.1.1 Backend (localhost:5000)          │
│  /api/auth    → Login / Register                            │
│  /api/kyc     → KYC document submission & admin review      │
│  /api/market  → Multi-source news + yFinance dashboard      │
│  /api/sentiment → NLTK sentiment analysis                   │
│  /api/trading → MT5 connect, trade history, ML predict      │
│  /api/powerbi → Public OHLCV + news-features endpoints      │
└──────────────┬──────────────────┬────────────────────────── ┘
               │                  │
               ▼                  ▼
   ┌─────────────────┐  ┌──────────────────────────┐
   │  MySQL Database  │  │  MetaTrader 5 Terminal   │
   │  (flask_app_db) │  │  (Windows Desktop App)   │
   │  Users / KYC /  │  │  mt5.initialize()        │
   │  MT5 Credentials│  │  mt5.login()             │
   └─────────────────┘  │  copy_rates_range()      │
                        │  order_send()            │
                        └──────────────────────────┘
               │
               ▼
   ┌─────────────────────────────────────────────────┐
   │  External Data Sources                          │
   │  yFinance (OHLCV + stock news)                  │
   │  Finviz / Yahoo Finance / CNBC /                │
   │  Benzinga / Motley Fool  (news scrapers)        │
   └─────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### Backend
| Package | Version | Purpose |
|---|---|---|
| **Flask** | 3.1.1 | Python micro web framework — HTTP server |
| **Flask-CORS** | 5.0.1 | Cross-Origin Resource Sharing — allows Angular on :4200 to talk to Flask on :5000 |
| **Flask-SQLAlchemy** | 3.1.1 | ORM layer — maps Python classes to MySQL tables |
| **Flask-JWT-Extended** | 4.6.0 | JSON Web Token auth — issues & validates stateless tokens |
| **PyMySQL** | 1.1.1 | Pure-Python MySQL driver |
| **cryptography** | latest | Required by PyMySQL for secure connections |
| **MetaTrader5** | latest | Windows-only Python bridge to the MT5 terminal |
| **yfinance** | latest | Yahoo Finance Python wrapper — stock OHLCV + news |
| **scikit-learn** | latest | Machine learning — `LinearRegression` model |
| **pandas** | latest | Data manipulation — `DataFrame` construction from MT5 rate arrays |
| **nltk** | latest | Natural Language Toolkit — VADER sentiment analysis |
| **requests** | latest | HTTP scraper for Finviz / Yahoo / CNBC / Benzinga / Motley Fool |
| **BeautifulSoup4** | latest | HTML parsing for news scrapers |
| **Werkzeug** | bundled | Password hashing (`generate_password_hash` / `check_password_hash`) |

### Frontend
| Package | Version | Purpose |
|---|---|---|
| **Angular** | 19 | TypeScript SPA framework |
| **Angular Router** | 19 | Client-side routing with route guards |
| **Angular HttpClient** | 19 | HTTP calls with functional interceptor support |
| **RxJS** | bundled | Reactive Observables — `BehaviorSubject`, `tap`, `map`, `take` |

### Database
| Component | Details |
|---|---|
| **MySQL** | Local instance, port 3306 |
| **Database name** | `flask_app_db` |
| **Tables auto-created** | `users`, `kyc_details`, `mt5_credentials`, `items` |

---

## 3. Directory Structure

```
flask-angular-app/
├── backend/
│   ├── run.py                   ← Entry point — starts Flask dev server
│   ├── requirements.txt         ← Python dependencies
│   └── app/
│       ├── __init__.py          ← App factory: create_app(), CORS, JWT, DB, Blueprints
│       ├── models.py            ← SQLAlchemy ORM models (User, KycDetails, Mt5Credential)
│       ├── auth.py              ← /api/auth/register, /api/auth/login
│       ├── kyc.py               ← /api/kyc/* — user submission + admin review
│       ├── market.py            ← /api/market/news/<ticker>, /api/market/dashboard/<ticker>
│       ├── sentiment.py         ← /api/sentiment/<ticker>
│       ├── trading.py           ← /api/trading/* — MT5 + Linear Regression ML
│       ├── powerbi.py           ← /api/powerbi/* — public Power BI endpoints
│       ├── scrapers.py          ← Finviz, Yahoo, CNBC, Benzinga, Motley Fool scrapers
│       └── routes.py            ← /api root blueprint
│
└── frontend/
    └── src/app/
        ├── app.config.ts        ← Angular providers (router, HttpClient, interceptors)
        ├── app.routes.ts        ← Route definitions + canActivate guard mapping
        ├── guards/
        │   └── kyc.guard.ts     ← KYC status gatekeeper for protected routes
        ├── services/
        │   ├── auth.ts          ← AuthService: login(), logout(), BehaviorSubject<User>
        │   ├── auth.interceptor.ts ← Functional HTTP interceptor — 401 auto-logout
        │   ├── trading.ts       ← TradingService: MT5 APIs + ML predict
        │   ├── market.ts        ← MarketService: news + dashboard
        │   ├── sentiment.ts     ← SentimentService
        │   ├── kyc.ts           ← KycService: submission + status
        │   └── api.service.ts   ← Generic base service
        └── pages/
            ├── login/           ← Login form page
            ├── register/        ← Registration form page
            ├── kyc-details/     ← Step 1: Personal info form
            ├── kyc-upload/      ← Step 2: Document upload
            ├── kyc-pending/     ← Waiting room — KYC submitted but not yet approved
            ├── admin-kyc/       ← Admin dashboard — view + approve KYC records
            ├── ml-trading/      ← AI Trading Bot — ML signals + trade execution
            ├── market-analysis/ ← Multi-source news + sentiment feed
            ├── sentiment-feed/  ← Dashboard with charts + historical OHLCV
            ├── trade-history/   ← MT5 closed deal history
            └── settings/        ← MT5 credential management
```

---

## 4. Backend — Flask Application

### 4.1 App Factory & Startup

**File:** `backend/run.py`
```python
from app import create_app
app = create_app()
if __name__ == "__main__":
    app.run(debug=True, port=5000, reloader_type='stat')
```

This is the **entry point**. Running `python run.py` starts the Flask development server on port 5000. The `reloader_type='stat'` flag uses file-stat polling instead of inotify (more stable on Windows).

**File:** `backend/app/__init__.py` — `create_app()` function

The `create_app()` function is the **Application Factory** pattern. Here is exactly what it does, step by step:

1. **Creates the Flask app instance** — `app = Flask(__name__)`
2. **Configures MySQL** — Sets `SQLALCHEMY_DATABASE_URI` to `mysql+pymysql://root:root@localhost:3306/flask_app_db`. The driver is `pymysql` (pure Python, no native MySQL client needed).
3. **Sets JWT secret** — `JWT_SECRET_KEY = "super-secret-dev-key"`. All tokens are signed with this key. Tokens expire after **30 days** (`JWT_ACCESS_TOKEN_EXPIRES = timedelta(days=30)`).
4. **Enables CORS** — `CORS(app, origins=["http://localhost:4200"])`. Only Angular's dev server is whitelisted. All other origins are blocked.
5. **Initializes SQLAlchemy** — `db.init_app(app)`. The global `db` object is now bound to this app.
6. **Initializes JWTManager** — `JWTManager(app)`. All `@jwt_required()` decorator checks are registered.
7. **Registers all Blueprints** — Each feature module is a Flask Blueprint, mounted at its URL prefix:
   - `auth_bp` → `/api/auth`
   - `kyc_bp` → `/api`
   - `market_bp` → `/api/market`
   - `sentiment_bp` → `/api/sentiment`
   - `trading_bp` → `/api/trading`
   - `powerbi_bp` → `/api/powerbi`
8. **Creates all DB tables** — Inside an app context: `db.create_all()`. This is **idempotent** — if tables already exist, nothing happens. If they don't exist, they are created from the SQLAlchemy model definitions.

---

### 4.2 Database Models

**File:** `backend/app/models.py`

#### `User` Table (`users`)
| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK | Auto-increment |
| `username` | VARCHAR(80) | Unique, not null |
| `password_hash` | VARCHAR(255) | Werkzeug PBKDF2/SHA256 hash — **never plain text** |
| `role` | VARCHAR(20) | `"user"` or `"admin"` |

**Key Methods:**
- `set_password(password)` — calls `generate_password_hash(password)` and stores the result in `password_hash`
- `check_password(password)` — calls `check_password_hash(stored_hash, password)` — returns `True`/`False`
- `to_dict()` — serializes to JSON, includes `kyc_status` from the related KYC record

#### `KycDetails` Table (`kyc_details`)
| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK | Auto-increment |
| `user_id` | INTEGER FK | Foreign key → `users.id`, unique (one KYC per user) |
| `first_name` | VARCHAR(100) | |
| `last_name` | VARCHAR(100) | |
| `phone` | VARCHAR(20) | |
| `address` | VARCHAR(255) | |
| `apt_no` | VARCHAR(50) | Nullable |
| `city`, `state`, `country`, `zipcode` | VARCHAR | |
| `document_path` | VARCHAR(255) | Absolute path to uploaded ID file on server |
| `status` | VARCHAR(20) | `Pending` → `Submitted` → `Approved` / `Rejected` |
| `created_at` | DATETIME | UTC timestamp |

The `user` ↔ `kyc` relationship is a **one-to-one backref**: `User.kyc` gives the KYC object; `KycDetails.user` gives the User. The cascade `"all, delete"` means deleting a user also deletes their KYC.

#### `Mt5Credential` Table (`mt5_credentials`)
| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK | Auto-increment |
| `user_id` | INTEGER FK | Foreign key → `users.id`, CASCADE DELETE, unique |
| `login_id` | BIGINT | MT5 account number (can be very large) |
| `password` | VARCHAR(255) | MT5 account password — stored as plain text (broker password) |
| `server` | VARCHAR(255) | Broker server name e.g. `"MetaQuotes-Demo"` |

---

### 4.3 Authentication System (Login & Logout)

**File:** `backend/app/auth.py`

#### Registration — `POST /api/auth/register`

```
Request  → { "username": "alice", "password": "secret", "is_admin": false }
Response → 201  { "message": "User created successfully", "user": {...} }
         → 400  { "message": "User already exists" }
         → 400  { "message": "Username and password are required" }
```

**Step-by-step flow:**
1. Extract `username`, `password`, `is_admin` from JSON body.
2. Validate both are non-empty.
3. Query `User.query.filter_by(username=username).first()` — if found, return 400.
4. Create `User` object with `role = "admin"` if `is_admin=True`, else `"user"`.
5. Call `new_user.set_password(password)` → Werkzeug hashes it with PBKDF2-HMAC-SHA256.
6. Add to session and commit: `db.session.add(new_user); db.session.commit()`.
7. Return serialized user dict with 201.

#### Login — `POST /api/auth/login`

```
Request  → { "username": "alice", "password": "secret" }
Response → 200  { "message": "Login successful", "access_token": "<JWT>", "user": {...} }
         → 401  { "message": "Invalid username or password" }
```

**Step-by-step flow:**
1. Extract `username`, `password` from JSON body.
2. Query: `User.query.filter_by(username=username).first()`.
3. Call `user.check_password(password)` — Werkzeug compares the PBKDF2 hash.
4. If valid: call `create_access_token(identity=str(user.id), additional_claims={"role": user.role})`.
   - The JWT payload contains: `sub` (user ID as string), `role` (user/admin), `exp` (expiry 30 days from now).
   - The token is signed with `JWT_SECRET_KEY` using HS256 algorithm.
5. Return 200 with the token and user dict.
6. If invalid: return 401 generically (no username enumeration).

#### Logout

> **There is no `/api/auth/logout` backend endpoint.**  
> This is intentional. JWT tokens are **stateless** — the server holds no session state.  
> Logout is handled entirely on the **frontend** by clearing `localStorage`.

---

### 4.4 KYC (Know Your Customer)

**File:** `backend/app/kyc.py`

The KYC system enforces a multi-step identity verification workflow before users can access trading features.

#### Status Lifecycle

```
None ──→ Pending ──→ Submitted ──→ Approved
                              └──→ Rejected
```

| Status | Meaning |
|---|---|
| `None` | User has never submitted KYC |
| `Pending` | Personal details submitted, no document yet |
| `Submitted` | Document uploaded, awaiting admin review |
| `Approved` | Admin approved — user can access all features |
| `Rejected` | Admin rejected — user must re-submit |

#### Endpoints

**`POST /api/kyc/details`** — Submit personal info (JWT required)
1. Extract `user_id` from JWT via `get_jwt_identity()`.
2. Check if KYC already exists for this user — if so, return 400.
3. Create `KycDetails` record with `status = "Pending"`.
4. Commit to DB and return the KYC record.

**`POST /api/kyc/upload`** — Upload ID document (JWT required)
1. Verify a KYC detail record exists (else return 400 asking to submit details first).
2. Check that a file was included in the `multipart/form-data` request under key `"document"`.
3. Sanitize the filename: `secure_filename(f"user_{user_id}_{timestamp}_{original_name}")`.
4. Save the file to `C:\Users\177r1\Desktop\SAm\KYC\`.
5. Update KYC `document_path` and set `status = "Submitted"`.

**`GET /api/kyc/status`** — Get own KYC status (JWT required)
Returns the current user's KYC status object.

**`GET /api/admin/kyc`** — Get all KYC records (Admin JWT required)
1. Calls `get_jwt()` to extract the JWT claims dict.
2. Checks `claims.get("role") != "admin"` — returns 403 if not admin.
3. Returns all KYC records.

**`POST /api/admin/kyc/<id>/approve`** — Approve a KYC (Admin JWT required)
Sets `status = "Approved"` for the specified KYC ID.

**`GET /api/admin/kyc/document/<id>`** — Download ID document (Admin JWT required)
Calls `send_file()` with the absolute path stored in `document_path`.

---

### 4.5 MetaTrader 5 Integration

**File:** `backend/app/trading.py`

MetaTrader 5 is a Windows desktop trading terminal. Python communicates with it through the **MetaTrader5 pip package**, which connects to the locally running MT5 desktop application via IPC (inter-process communication). **MT5 must be running on the same machine as Flask.**

#### MT5 Credential Management

**`GET/POST /api/trading/credentials`** (JWT required)

- `GET` — returns the stored MT5 credentials (login_id, password, server) from the `mt5_credentials` DB table.
- `POST` — upserts the credentials. If a record exists for the current user, it updates. If not, it inserts a new one.

#### MT5 Connection — `GET /api/trading/connect` (JWT required)

Exact steps:
1. Load credentials from `mt5_credentials` DB for the current user's `user_id`.
2. Call `mt5.initialize()` — this finds and connects to the running MT5 terminal process.
3. Call `mt5.login(login=login_id, password=password, server=server)` — authenticates with the broker.
4. If authorized: call `mt5.account_info()` to get Balance, Equity, Profit, Currency, Broker name.
5. Return all account data as JSON.
6. If `mt5.initialize()` fails: the MT5 desktop terminal is not running.
7. If `mt5.login()` fails: wrong credentials — call `mt5.shutdown()` to release the IPC lock.

#### Trade History — `GET /api/trading/history` (JWT required)

1. Connect to MT5 (same init + login flow as above).
2. Set `utc_from = datetime(2000, 1, 1)` (all history) and `utc_to = now + 24h` (generous buffer for broker timezone).
3. Call `mt5.history_deals_get(utc_from, utc_to)` — returns a list of all closed deals.
4. For each deal, build a dict with ticket, time, type, entry, volume, price, symbol, comment.
5. **Timezone adjustment:** `time = int(deal.time) - 10800` — the broker server runs on UTC+3 (EET standard). Subtracting 10,800 seconds (3 hours) converts to UTC. The frontend then converts UTC unix timestamps to the user's local timezone.
6. **Profit calculation:** `deal.profit + commission + swap + fee` — all components are summed for total P&L per deal.
7. Sort descending by time (newest first).

#### Historical Rates — `GET /api/trading/rates/<ticker>` (JWT required)

1. Connect to MT5.
2. Set `utc_from = now - 7 days` and `utc_to = now`.
3. Call `mt5.copy_rates_range(ticker, mt5.TIMEFRAME_M5, utc_from, utc_to)`.
   - `TIMEFRAME_M5` = 5-minute candles.
   - Returns a numpy structured array of OHLCV data.
4. Convert each rate to a dict: `time` (UTC unix int), `open`, `high`, `low`, `close`, `tick_volume`.
5. Sort descending, return as JSON. Frontend renders this into a candlestick or table.

#### Trade Execution — `POST /api/trading/execute` (JWT required)

```
Request → { "ticker": "EURUSD", "action": "BUY", "volume": 0.1, "sl": 1.0800, "tp": 1.1200 }
```

1. Connect to MT5 (init + login).
2. Validate `ticker` and `action` (must be `BUY` or `SELL`).
3. Call `mt5.symbol_info(ticker)` — check the symbol exists and is visible on the platform.
4. Call `mt5.symbol_info_tick(ticker)` — get the current live bid/ask prices.
   - **BUY** uses `tick.ask` (you buy at the broker's ask price).
   - **SELL** uses `tick.bid` (you sell at the broker's bid price).
5. Detect the broker's order filling mode via `symbol_info.filling_mode` bitmask:
   - Bit 1 set → FOK (Fill or Kill) = constant 0
   - Bit 2 set → IOC (Immediate or Cancel) = constant 1
   - Neither → RETURN = constant 2 (safe fallback for all brokers)
6. Build the `request_payload` dict and call `mt5.order_send(request_payload)`.
7. Check `result.retcode == mt5.TRADE_RETCODE_DONE` for success.
8. `magic = 234000` tags all bot orders for identification in the history.

---

### 4.6 Machine Learning — Linear Regression

**File:** `backend/app/trading.py` — `ml_predict()` endpoint  
**Route:** `GET /api/trading/ml/predict/<ticker>` (JWT required)

This is a **dual-model ML system** that combines price-based regression with news sentiment scoring.

#### Model 1: Historical Price Analysis (scikit-learn LinearRegression)

**Data source:** MetaTrader 5 — 7 days of 5-minute OHLCV candles.

**Step-by-step:**

1. **Connect to MT5** and fetch 7 days of M5 candles:
   ```python
   rates = mt5.copy_rates_range(ticker, mt5.TIMEFRAME_M5, utc_from, utc_to)
   ```
2. **Build a DataFrame:**
   ```python
   df = pd.DataFrame(rates)
   df['time'] = pd.to_datetime(df['time'], unit='s')
   ```
3. **Create the target variable** — the *next* candle's close price:
   ```python
   df['target'] = df['close'].shift(-1)   # shift all values up by 1 row
   df = df.dropna()                        # drop the last row (no target)
   ```
   This means: for each candle, the model learns "given these OHLCV values, what will the NEXT close be?"
4. **Define features (X) and target (y):**
   ```python
   X = df[['open', 'high', 'low', 'close', 'tick_volume']]
   y = df['target']
   ```
5. **Train the model:**
   ```python
   model = LinearRegression()
   model.fit(X, y)
   ```
   LinearRegression finds the hyperplane `y = w₁·open + w₂·high + w₃·low + w₄·close + w₅·volume + b` that minimizes mean squared error across all training candles.
6. **Predict the next close:**
   ```python
   last_candle = df.iloc[-1][['open', 'high', 'low', 'close', 'tick_volume']].values.reshape(1, -1)
   pred = model.predict(last_candle)[0]
   ```
7. **Generate signal:**
   - `pred > current_price` → **BUY** signal (model predicts price going up)
   - `pred < current_price` → **SELL** signal (model predicts price going down)
8. **Calculate confidence:**
   ```python
   diff = pred - current_price
   confidence = min(abs(diff / current_price) * 10000, 100.0)
   ```
   This scales the percentage difference into a 0–100 range. A 0.01% move = confidence of 1.0.

> **Note:** LinearRegression is trained fresh on every request — there is no model persistence. This means each call re-trains on the latest 7 days of data, ensuring the model always reflects current market conditions.

#### Model 2: News Sentiment (NLTK VADER)

**Data source:** Finviz + Yahoo Finance scrapers running concurrently.

1. **Fetch news concurrently** using `ThreadPoolExecutor(max_workers=5)`.
2. For each headline: run `SentimentIntensityAnalyzer().polarity_scores(headline)`.
3. VADER returns `{"compound": float, "pos": float, "neg": float, "neu": float}`.
   - `compound >= 0.05` → **Positive** (BUY signal)
   - `compound <= -0.05` → **Negative** (SELL signal)
   - Between → **Neutral**
4. Count positives, negatives, neutrals across all headlines.
5. The dominant category determines the `news_signal`.
6. Confidence = `(dominant_count / total_articles) * 100`.

#### Combined Response

```json
{
  "ticker": "AAPL",
  "historical_model": {
    "signal": "BUY",
    "confidence": 73.4,
    "predicted_close": 189.23,
    "current_price": 188.95
  },
  "news_model": {
    "signal": "BUY",
    "confidence": 68.0,
    "articles_analyzed": 25
  }
}
```

---

### 4.7 News Sentiment — NLTK VADER

VADER (**Valence Aware Dictionary and sEntiment Reasoner**) is a lexicon-and-rule-based sentiment analysis tool specifically tuned for social media and financial news.

**How it works:**
1. Pre-trained dictionary of ~7,500 words, each scored from -4 (most negative) to +4 (most positive) e.g.: `"crash" = -2.5`, `"soaring" = +2.8`, `"announced" = 0.1`.
2. Rules adjust scores for: capitalization (`CRASH` more negative than `crash`), punctuation (`crash!!!` more intense), negation (`not good` is negative), and degree modifiers (`very good` more positive than `good`).
3. Output is a normalized compound score in [-1, +1].
4. Thresholds: `>= 0.05` = Positive, `<= -0.05` = Negative, in-between = Neutral.

**Pre-loading:** `nltk.download('vader_lexicon', quiet=True)` is called at Flask startup so the lexicon is available immediately when the first request arrives.

---

### 4.8 News Scrapers

**File:** `backend/app/scrapers.py`

All scrapers use `requests.get()` with a Chrome user-agent string to avoid bot blocking, and `BeautifulSoup` to parse HTML.

| Function | Source | Method | Filter |
|---|---|---|---|
| `get_finviz_news(ticker)` | finviz.com | Parses `#news-table` TR elements | Only articles within last 24 hours |
| `get_yahoo_news(ticker)` | finance.yahoo.com | Finds `<a class="subtle-link">` tags | Only articles with "hour/minute/now" in timestamp |
| `get_cnbc_news(ticker)` | cnbc.com/quotes/ | Selects `a.LatestNews-headline` | All (no time filter) |
| `get_benzinga_news(ticker)` | benzinga.com/quote/ | Finds all `<a>` with `/news/` in href | Min headline length of 20 chars |
| `get_fool_news(ticker)` | fool.com/quote/nasdaq/ | Parses article container `div > a` | Excludes author/navigation links |

All scrapers return a list of dicts: `{"source", "headline", "url", "metadata"}`.

All scrapers are called **concurrently** using `ThreadPoolExecutor(max_workers=5)` — 5 scrapers run in parallel, so total latency ≈ the slowest single scraper, not the sum of all.

---

### 4.9 Market Dashboard Endpoint

**Route:** `GET /api/market/dashboard/<ticker>` (JWT required)

This is the most complex endpoint. It does three things in one call:

1. **Scrapes all 5 news sources concurrently** (same ThreadPoolExecutor pattern).
2. **Fetches from yFinance:**
   - 2-day daily history → calculates today's change vs yesterday's close.
   - 30-day daily OHLCV history → returned as structured array with `candle_color`, `change_pct`.
3. **Runs NLTK VADER** on every scraped headline:
   - Aggregates `positive_count`, `negative_count`, `neutral_count`.
   - Calculates `avg_confidence` and `overall` sentiment direction.
   - Each news item is enriched with the ticker's live quote data (`open`, `close`, `volume`) so it can be used directly in a trade decision table.

Returns: `{ quote, sentiment_summary, structured (news+sentiment rows), historical (30-day OHLCV) }`.

---

### 4.10 Power BI Data Endpoints

**File:** `backend/app/powerbi.py`  
**Note: These endpoints require NO JWT token.** They are intentionally public so Power BI's Web connector can call them directly without authentication configuration.

#### `GET /api/powerbi/news-features/<ticker>`

Returns one JSON row per news headline with VADER sentiment scores:
```json
{
  "ticker": "MSFT",
  "record_count": 18,
  "generated_at": "2026-04-13T22:00:00Z",
  "data": [
    {
      "ticker": "MSFT", "headline": "...", "source": "...",
      "published_at": "...", "scraped_at": "...",
      "compound": 0.6249, "positive": 0.523, "negative": 0.0, "neutral": 0.477,
      "sentiment_label": "Positive", "sentiment_numeric": 1
    }
  ]
}
```

#### `GET /api/powerbi/historical/<ticker>?days=30&interval=5m`

Returns flat OHLCV rows from yFinance:

| Parameter | Default | Options |
|---|---|---|
| `days` | 30 | Any integer |
| `interval` | `1d` | `5m`, `15m`, `1h`, `1d` |

Each row includes: `ticker`, `date`, `open`, `high`, `low`, `close`, `volume`, `candle_color`, `body_size`, `upper_wick`, `lower_wick`, `change_pct`.

> **yFinance limitation:** 5-minute interval data is only available for the past ~60 days, and the usable window is typically 7 days. For a true 30-day chart, use `interval=15m` or `interval=1h`.

**Power BI Connection URL (30 days, 5-min chart, AAPL):**
```
http://localhost:5000/api/powerbi/historical/AAPL?days=30&interval=5m
```

---

## 5. Frontend — Angular 19 Application

### 5.1 App Bootstrap & Configuration

**File:** `frontend/src/app/app.config.ts`

Angular 19 uses the **standalone component** model — there is no `AppModule`. Bootstrap happens via `bootstrapApplication()` with an `ApplicationConfig` object.

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),          // Global error handler
    provideZoneChangeDetection({ eventCoalescing: true }), // Performance: batch change detection
    provideRouter(routes),                          // Client-side router
    provideHttpClient(withInterceptors([authInterceptor])) // HttpClient + JWT interceptor
  ]
};
```

**`withInterceptors([authInterceptor])`** — This registers the functional HTTP interceptor. Every outgoing `HttpClient` request passes through `authInterceptor` before reaching the server.

---

### 5.2 Routing & Route Guards

**File:** `frontend/src/app/app.routes.ts`

```
/login           → Login component        (public)
/register        → Register component     (public)
/kyc-details     → KycDetails component   (public — redirect target after login if KYC not done)
/kyc-upload      → KycUpload component    (public)
/kyc-pending     → KycPending component   (public — waiting room)
/admin-kyc       → AdminKyc component     (public — admin only, enforced by guard logic inside)
/ml-trading      → MlTrading             [canActivate: kycGuard]
/market-analysis → MarketAnalysis        [canActivate: kycGuard]
/sentiment-feed  → SentimentFeed         [canActivate: kycGuard]
/trade-history   → TradeHistory          [canActivate: kycGuard]
/settings        → Settings             [canActivate: kycGuard]
/               → redirectTo: /market-analysis
```

#### KYC Guard — `kyc.guard.ts`

The `kycGuard` is a **functional route guard** (`CanActivateFn`) that protects all trading/analysis pages.

**Decision tree:**
```
Is user an admin?
  YES → Allow access (admins bypass KYC)
   └─ Is URL /dashboard? → Redirect to /admin-kyc
  NO ↓
Is user logged in (BehaviorSubject has a user)?
  NO → Redirect to /login
  YES ↓
What is user.kyc_status?
  "Approved"   → Allow access ✅
  "Submitted"  → Redirect to /kyc-pending (waiting for admin review)
  anything else → Redirect to /kyc-details (user must submit KYC)
```

---

### 5.3 Auth Service (Login / Logout Flow)

**File:** `frontend/src/app/services/auth.ts`

The `AuthService` uses RxJS `BehaviorSubject<User | null>` as a reactive state store.

#### BehaviorSubject

`BehaviorSubject` is an RxJS stream that always holds the **current value** and emits it immediately to any new subscriber. This allows any component to subscribe to `currentUser$` and receive the logged-in user object reactively.

#### Constructor — Session Persistence

```typescript
constructor() {
  const user = localStorage.getItem('user');
  if (user) {
    this.currentUserSubject.next(JSON.parse(user));
  }
}
```
On app startup, if a `user` object is found in `localStorage` (from a previous session), it is immediately loaded into the BehaviorSubject. This means the user stays "logged in" across browser refreshes until they explicitly logout or the token expires.

#### Login Flow (Frontend)

```typescript
login(data: any) {
  return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
    tap(response => {
      localStorage.setItem('access_token', response.access_token);
      localStorage.setItem('user', JSON.stringify(response.user));
      this.currentUserSubject.next(response.user);
    })
  );
}
```

`tap()` is a side-effect operator — when the HTTP call succeeds:
1. Saves `access_token` to `localStorage` (persists across sessions).
2. Saves the `user` object to `localStorage`.
3. Pushes the user into the BehaviorSubject — all subscribed components immediately know the user is logged in.

The login **component** then redirects based on KYC status:
- `kyc_status !== "Approved"` → `/kyc-details`
- `kyc_status === "Approved"` → `/market-analysis`
- Admin → `/admin-kyc`

#### Logout Flow (Frontend)

```typescript
logout() {
  localStorage.removeItem('access_token');
  localStorage.removeItem('user');
  this.currentUserSubject.next(null);
}
```

1. Removes both keys from `localStorage`.
2. Pushes `null` into the BehaviorSubject — all subscribed components immediately know the user is logged out.
3. The calling code then navigates to `/login`.
4. **No server call needed** — JWT is stateless; discarding the token client-side is sufficient.

---

### 5.4 JWT HTTP Interceptor

**File:** `frontend/src/app/services/auth.interceptor.ts`

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
```

This interceptor does **NOT** attach the token to requests automatically (note: no `clone` of the request). Instead, each service manually adds the `Authorization: Bearer <token>` header via its own `getHeaders()` method.

What the interceptor **does** do is catch **any** 401 Unauthorized response globally. If a request returns 401 (e.g., token expired), it:
1. Calls `authService.logout()` — clears localStorage and BehaviorSubject.
2. Navigates to `/login`.

This ensures users are automatically logged out when their JWT expires (after 30 days), without needing explicit checks in every component.

---

### 5.5 Feature Services

Each service follows the same pattern: `inject(HttpClient)`, build an `Authorization` header from `localStorage.getItem('access_token')`, make typed HTTP calls.

| Service | File | Endpoints Covered |
|---|---|---|
| `AuthService` | `auth.ts` | `/api/auth/register`, `/api/auth/login` |
| `TradingService` | `trading.ts` | `/api/trading/credentials`, `/connect`, `/history`, `/rates/<ticker>`, `/execute`, `/ml/predict/<ticker>` |
| `MarketService` | `market.ts` | `/api/market/news/<ticker>`, `/api/market/dashboard/<ticker>` |
| `SentimentService` | `sentiment.ts` | `/api/sentiment/<ticker>` |
| `KycService` | `kyc.ts` | `/api/kyc/details`, `/api/kyc/upload`, `/api/kyc/status`, `/api/admin/kyc` |

---

### 5.6 Pages Overview

| Page | Route | Access | Description |
|---|---|---|---|
| `Login` | `/login` | Public | Email + password form, calls `AuthService.login()` |
| `Register` | `/register` | Public | Username + password + optional admin flag |
| `KycDetails` | `/kyc-details` | Public | Step 1: personal info form (name, address, phone) |
| `KycUpload` | `/kyc-upload` | Public | Step 2: file upload for government ID |
| `KycPending` | `/kyc-pending` | Public | Informational waiting page — shown after document upload |
| `AdminKyc` | `/admin-kyc` | Admin only | Table of all KYC submissions + approve buttons + document viewer |
| `MlTrading` | `/ml-trading` | KYC Approved | ML signal display, trade execution form, MT5 account info |
| `MarketAnalysis` | `/market-analysis` | KYC Approved | Multi-source news feed with NLTK sentiment labels |
| `SentimentFeed` | `/sentiment-feed` | KYC Approved | Dashboard with stock selector, sentiment bars, 30-day OHLCV table |
| `TradeHistory` | `/trade-history` | KYC Approved | Full MT5 closed deals history with P&L |
| `Settings` | `/settings` | KYC Approved | MT5 credential form + connection test |

---

## 6. API Reference

### Auth
| Method | URL | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Create a new user |
| POST | `/api/auth/login` | None | Authenticate, receive JWT |

### KYC
| Method | URL | Auth | Description |
|---|---|---|---|
| POST | `/api/kyc/details` | JWT | Submit personal details |
| POST | `/api/kyc/upload` | JWT | Upload identity document |
| GET | `/api/kyc/status` | JWT | Get own KYC status |
| GET | `/api/admin/kyc` | JWT (admin) | List all KYC records |
| POST | `/api/admin/kyc/<id>/approve` | JWT (admin) | Approve a KYC |
| GET | `/api/admin/kyc/document/<id>` | JWT (admin) | Download uploaded document |

### Market & Sentiment
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | `/api/market/news/<ticker>` | JWT | Multi-source raw news |
| GET | `/api/market/dashboard/<ticker>` | JWT | Quote + sentiment summary + 30-day history |
| GET | `/api/sentiment/<ticker>` | JWT | NLTK-scored news list |

### Trading & ML
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | `/api/trading/credentials` | JWT | Get stored MT5 credentials |
| POST | `/api/trading/credentials` | JWT | Save/update MT5 credentials |
| GET | `/api/trading/connect` | JWT | Connect to MT5, return account info |
| GET | `/api/trading/history` | JWT | All closed deals from MT5 |
| GET | `/api/trading/rates/<ticker>` | JWT | 7-day 5-minute OHLCV from MT5 |
| POST | `/api/trading/execute` | JWT | Place a market order |
| GET | `/api/trading/ml/predict/<ticker>` | JWT | Linear regression + VADER prediction |

### Power BI (Public)
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | `/api/powerbi/historical/<ticker>?days=30&interval=5m` | None | Flat OHLCV from yFinance |
| GET | `/api/powerbi/news-features/<ticker>` | None | Headline + VADER scores |

---

## 7. Data Flow Diagrams

### Login Flow
```
User submits form
       ↓
Angular Login component calls AuthService.login({ username, password })
       ↓
POST http://localhost:5000/api/auth/login
       ↓
Flask: User.query.filter_by(username=...).first()
Flask: user.check_password(password)  ← Werkzeug PBKDF2 hash check
       ↓
Flask: create_access_token(identity=user.id, claims={role})  ← JWT signed HS256
       ↓
Response: { access_token, user: { id, username, role, kyc_status } }
       ↓
AuthService.tap(): localStorage.setItem('access_token', ...)
                   localStorage.setItem('user', ...)
                   BehaviorSubject.next(user)
       ↓
Component navigates based on kyc_status
```

### ML Prediction Flow
```
User selects ticker, clicks "Analyze"
       ↓
TradingService.getMlPredictions(ticker)
GET /api/trading/ml/predict/AAPL  [Authorization: Bearer <token>]
       ↓
Flask: Validates JWT
       ↓
Flask: mt5.initialize() + mt5.login() using DB credentials
Flask: copy_rates_range(TIMEFRAME_M5, last 7 days)  → ~2016 candles
       ↓
pandas DataFrame:
  - column 'target' = close.shift(-1)  [next candle's close]
  - X = [open, high, low, close, tick_volume]
  - y = target
       ↓
LinearRegression().fit(X, y)
predict(last_row)  → predicted_close
       ↓
signal = "BUY" if predicted > current else "SELL"
confidence = min(|diff/current| × 10000, 100)
       ↓
Concurrently: Finviz + Yahoo news → VADER scores
       ↓
Response: { historical_model: {...}, news_model: {...} }
       ↓
Frontend displays signals, confidence bars, and recommendation
```

### Trade Execution Flow
```
User selects ticker + BUY/SELL + volume, clicks "Execute"
       ↓
POST /api/trading/execute  { ticker, action, volume, sl, tp }
       ↓
Flask: mt5.initialize() + mt5.login()
Flask: mt5.symbol_info(ticker)  → validate symbol exists
Flask: mt5.symbol_info_tick(ticker)  → get live ask/bid price
Flask: detect filling_mode from symbol_info bitmask
       ↓
mt5.order_send({
  action: TRADE_ACTION_DEAL,
  symbol: ticker,
  volume: volume,
  type: ORDER_TYPE_BUY or ORDER_TYPE_SELL,
  price: ask or bid,
  deviation: 20,
  magic: 234000,
  type_time: ORDER_TIME_GTC,
  type_filling: FOK/IOC/RETURN
})
       ↓
result.retcode == TRADE_RETCODE_DONE?
  YES → 200 "Successfully executed BUY 0.1 lots of EURUSD"
  NO  → 400 "Order failed! Error: <reason> (<code>)"
```

---

## 8. Setup & Running Locally

### Prerequisites
- **Python 3.10+** (Windows)
- **Node.js 18+** and npm
- **MySQL 8.x** running locally on port 3306
- **MetaTrader 5** desktop terminal installed and running (for trading features)
- Angular CLI: `npm install -g @angular/cli`

### Backend Setup

```powershell
# Navigate to backend
cd flask-angular-app\backend

# Create and activate virtual environment
python -m venv venv
.\venv\Scripts\Activate.ps1

# Install dependencies
pip install -r requirements.txt

# Install additional required packages
pip install MetaTrader5 yfinance nltk requests beautifulsoup4

# Create MySQL database
mysql -u root -p -e "CREATE DATABASE flask_app_db;"

# Start Flask server (auto-creates tables)
python run.py
```

Flask will start at: **http://localhost:5000**

### Frontend Setup

```powershell
# Navigate to frontend
cd flask-angular-app\frontend

# Install dependencies
npm install

# Start Angular dev server
ng serve
```

Angular will start at: **http://localhost:4200**

### First-Time Setup Checklist

1. Open http://localhost:4200/register
2. Create a user account (check "is admin" for the first admin user)
3. Log in with the admin account → you will be redirected to `/admin-kyc`
4. Create a regular user account and log in
5. Complete KYC: fill personal details → upload a document
6. As admin: go to `/admin-kyc` → find the submission → click Approve
7. Log back in as the regular user → KYC status is now Approved → access all features
8. Go to Settings → enter your MT5 credentials → click "Test Connection"

---

## 9. Security Design

| Concern | Implementation |
|---|---|
| Password storage | Werkzeug PBKDF2-HMAC-SHA256 — never plain text |
| Token authentication | JWT (HS256) — stateless, 30-day expiry |
| Token transport | `Authorization: Bearer <token>` header — not in cookies |
| CORS | Only `http://localhost:4200` is whitelisted |
| Admin authorization | JWT claims checked via `get_jwt()["role"]` on every admin route |
| Route protection (frontend) | `kycGuard` — checks KYC status before allowing route activation |
| Auto-logout | 401 interceptor clears session and redirects to `/login` |
| File upload | `secure_filename()` sanitizes uploaded filenames; files stored outside web root |
| MT5 credential storage | Stored in MySQL (not in config files); retrieved per-request from DB |

---

## 10. Known Limitations & Notes

1. **MT5 Windows-only:** The `MetaTrader5` Python package only works on Windows and requires the MT5 desktop terminal to be open. On macOS/Linux, all MT5 endpoints will fail at `mt5.initialize()`.

2. **No token refresh:** JWT tokens are valid for 30 days. There is no refresh token mechanism — users are simply logged out when tokens expire.

3. **Power BI endpoints are unauthenticated:** The `/api/powerbi/*` endpoints have no JWT protection. Anyone with network access to port 5000 can access them. This is intentional for Power BI compatibility.

4. **MT5 credentials stored as plain text:** The MT5 broker password is stored unencrypted in MySQL. This is a known limitation — production systems should use field-level encryption.

5. **ML model not persisted:** The LinearRegression model is retrained on every inference call. This is fast for small datasets (~2000 rows) but not scalable. For production, consider serializing trained models with `joblib`.

6. **News scrapers are fragile:** HTML-based scrapers break when source websites change their markup. If news data stops appearing, one or more scrapers likely needs to be updated.

7. **yFinance 5m interval limit:** Yahoo Finance only provides 5-minute OHLCV data for the past ~60 days (practical limit ~7 days). For Power BI 30-day, 5-min charts, use `interval=15m` instead.

8. **KYC document path is hardcoded:** `UPLOAD_FOLDER = r"C:\Users\177r1\Desktop\SAm\KYC"` — this must be updated before deploying on a different machine.

---

*Generated: April 2026 | Stack: Flask 3.1.1 + Angular 19 + MySQL + MT5 + scikit-learn + NLTK*
