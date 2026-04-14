from flask import Blueprint, jsonify, request
from app import db
from app.models import Item

api_bp = Blueprint("api", __name__)


@api_bp.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "healthy", "message": "Flask API is running!"})


@api_bp.route("/items", methods=["GET"])
def get_items():
    items = Item.query.all()
    return jsonify([item.to_dict() for item in items])


@api_bp.route("/items", methods=["POST"])
def create_item():
    data = request.get_json()
    item = Item(name=data["name"], description=data.get("description"))
    db.session.add(item)
    db.session.commit()
    return jsonify(item.to_dict()), 201


@api_bp.route("/items/<int:item_id>", methods=["DELETE"])
def delete_item(item_id):
    item = Item.query.get_or_404(item_id)
    db.session.delete(item)
    db.session.commit()
    return jsonify({"message": "Item deleted"}), 200
