from app import db
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

class Item(db.Model):
    __tablename__ = "items"

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    name = db.Column(db.String(100), nullable=False)
    description = db.Column(db.String(255), nullable=True)

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "description": self.description,
        }

class User(db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.String(20), nullable=False, default="user")

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def to_dict(self):
        return {
            "id": self.id,
            "username": self.username,
            "role": self.role,
            "kyc_status": self.kyc.status if self.kyc else "Pending"
        }

class KycDetails(db.Model):
    __tablename__ = "kyc_details"
    
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), unique=True, nullable=False)
    first_name = db.Column(db.String(100), nullable=False)
    last_name = db.Column(db.String(100), nullable=False)
    phone = db.Column(db.String(20), nullable=False)
    address = db.Column(db.String(255), nullable=False)
    apt_no = db.Column(db.String(50), nullable=True)
    city = db.Column(db.String(100), nullable=False)
    state = db.Column(db.String(100), nullable=False)
    country = db.Column(db.String(100), nullable=False)
    zipcode = db.Column(db.String(20), nullable=False)
    document_path = db.Column(db.String(255), nullable=True)
    status = db.Column(db.String(20), nullable=False, default="Pending") # Pending, Submitted, Approved, Rejected
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationship back to user
    user = db.relationship('User', backref=db.backref('kyc', uselist=False, cascade="all, delete"))

    def to_dict(self):
        return {
            "id": self.id,
            "user_id": self.user_id,
            "username": self.user.username if self.user else "",
            "first_name": self.first_name,
            "last_name": self.last_name,
            "phone": self.phone,
            "address": self.address,
            "apt_no": self.apt_no,
            "city": self.city,
            "state": self.state,
            "country": self.country,
            "zipcode": self.zipcode,
            "document_path": self.document_path,
            "status": self.status,
            "date": self.created_at.strftime("%Y-%m-%d %H:%M:%S")
        }

class Mt5Credential(db.Model):
    __tablename__ = 'mt5_credentials'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False, unique=True)
    login_id = db.Column(db.BigInteger, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    server = db.Column(db.String(255), nullable=False)

    def to_dict(self):
        return {
            "login_id": self.login_id,
            "password": self.password,
            "server": self.server
        }
