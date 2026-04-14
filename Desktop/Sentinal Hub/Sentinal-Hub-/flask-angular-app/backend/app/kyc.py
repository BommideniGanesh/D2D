import os
import time
from flask import Blueprint, request, jsonify, send_file
from flask_jwt_extended import jwt_required, get_jwt_identity, get_jwt
from werkzeug.utils import secure_filename
from app.models import User, KycDetails
from app import db

kyc_bp = Blueprint('kyc_bp', __name__)

UPLOAD_FOLDER = r"C:\Users\177r1\Desktop\SAm\KYC"

# Ensure upload directory exists
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@kyc_bp.route('/kyc/details', methods=['POST'])
@jwt_required()
def submit_details():
    user_id = get_jwt_identity()
    data = request.get_json()
    
    # Check if KYC already exists
    kyc = KycDetails.query.filter_by(user_id=user_id).first()
    if kyc:
        return jsonify({"msg": "KYC details already submitted."}), 400
        
    try:
        new_kyc = KycDetails(
            user_id=user_id,
            first_name=data.get('first_name'),
            last_name=data.get('last_name'),
            phone=data.get('phone'),
            address=data.get('address'),
            apt_no=data.get('apt_no', ''),
            city=data.get('city'),
            state=data.get('state'),
            country=data.get('country'),
            zipcode=data.get('zipcode'),
            status='Pending' # Details submitted, waiting for document
        )
        db.session.add(new_kyc)
        db.session.commit()
        return jsonify({"msg": "Details submitted successfully", "kyc": new_kyc.to_dict()}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"msg": f"Error saving details: {str(e)}"}), 500

@kyc_bp.route('/kyc/upload', methods=['POST'])
@jwt_required()
def upload_document():
    user_id = get_jwt_identity()
    kyc = KycDetails.query.filter_by(user_id=user_id).first()
    
    if not kyc:
        return jsonify({"msg": "Please submit personal details first."}), 400
        
    if 'document' not in request.files:
        return jsonify({"msg": "No document provided"}), 400
        
    file = request.files['document']
    if file.filename == '':
        return jsonify({"msg": "No file selected"}), 400
        
    if file:
        filename = secure_filename(f"user_{user_id}_{int(time.time())}_{file.filename}")
        file_path = os.path.join(UPLOAD_FOLDER, filename)
        file.save(file_path)
        
        kyc.document_path = file_path
        kyc.status = 'Submitted' # Ready for admin review
        db.session.commit()
        return jsonify({"msg": "Document uploaded successfully", "kyc": kyc.to_dict()}), 200

@kyc_bp.route('/kyc/status', methods=['GET'])
@jwt_required()
def get_kyc_status():
    user_id = get_jwt_identity()
    kyc = KycDetails.query.filter_by(user_id=user_id).first()
    if not kyc:
        return jsonify({"status": "None"}), 200
    return jsonify({"status": kyc.status, "kyc": kyc.to_dict()}), 200

# --- Admin Routes ---
@kyc_bp.route('/admin/kyc', methods=['GET'])
@jwt_required()
def get_all_kyc():
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({"msg": "Admin access required"}), 403
        
    kycs = KycDetails.query.all()
    return jsonify([k.to_dict() for k in kycs]), 200

@kyc_bp.route('/admin/kyc/<int:kyc_id>/approve', methods=['POST'])
@jwt_required()
def approve_kyc(kyc_id):
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({"msg": "Admin access required"}), 403
        
    kyc = KycDetails.query.get(kyc_id)
    if not kyc:
        return jsonify({"msg": "KYC not found"}), 404
        
    kyc.status = 'Approved'
    db.session.commit()
    return jsonify({"msg": "KYC Approved", "kyc": kyc.to_dict()}), 200

@kyc_bp.route('/admin/kyc/document/<int:kyc_id>', methods=['GET'])
@jwt_required()
def get_document(kyc_id):
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({"msg": "Admin access required"}), 403
        
    kyc = KycDetails.query.get(kyc_id)
    if not kyc or not kyc.document_path or not os.path.exists(kyc.document_path):
        return jsonify({"msg": "Document not found"}), 404
        
    return send_file(kyc.document_path)
