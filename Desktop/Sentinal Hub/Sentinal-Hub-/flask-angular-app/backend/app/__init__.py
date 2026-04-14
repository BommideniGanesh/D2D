from flask import Flask
from flask_cors import CORS
from flask_sqlalchemy import SQLAlchemy
from flask_jwt_extended import JWTManager
from datetime import timedelta

db = SQLAlchemy()


def create_app():
    app = Flask(__name__)

    # MySQL connection config — update YOUR_PASSWORD with your root password
    app.config["SQLALCHEMY_DATABASE_URI"] = (
        "mysql+pymysql://root:root@localhost:3306/flask_app_db"
    )
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
    app.config["JWT_SECRET_KEY"] = "super-secret-dev-key"
    app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(days=30)
    CORS(app, origins=["http://localhost:4200"])
    db.init_app(app)
    JWTManager(app)

    from app.routes import api_bp
    from app.auth import auth_bp
    from app.kyc import kyc_bp
    from app.market import market_bp
    from app.sentiment import sentiment_bp
    from app.trading import trading_bp
    from app.powerbi import powerbi_bp

    app.register_blueprint(api_bp, url_prefix="/api")
    app.register_blueprint(auth_bp, url_prefix="/api/auth")
    app.register_blueprint(kyc_bp, url_prefix="/api")
    app.register_blueprint(market_bp, url_prefix="/api/market")
    app.register_blueprint(sentiment_bp, url_prefix="/api/sentiment")
    app.register_blueprint(trading_bp, url_prefix="/api/trading")
    app.register_blueprint(powerbi_bp, url_prefix="/api/powerbi")

    # Create all tables on startup
    with app.app_context():
        from app import models  # noqa: F401

        db.create_all()

    return app
