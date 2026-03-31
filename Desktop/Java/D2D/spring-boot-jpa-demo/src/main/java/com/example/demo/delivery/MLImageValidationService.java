package com.example.demo.delivery;

import org.springframework.stereotype.Service;

@Service
public class MLImageValidationService {

    /**
     * Mocks an ML model analyzing a delivery drop-off photo.
     * Rejects images if the URL contains 'blur', 'invalid', or 'dark'.
     */
    public boolean validateDeliveryImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        String lowerUrl = imageUrl.toLowerCase();
        if (lowerUrl.contains("blur") || lowerUrl.contains("invalid") || lowerUrl.contains("dark")) {
            return false; // ML Model determined the photo is not sufficient
        }
        
        return true; // ML Model passed the photo
    }
}
