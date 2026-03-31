package com.example.demo.delivery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MLImageValidationServiceTest {

    private final MLImageValidationService mlService = new MLImageValidationService();

    @Test
    public void testValidImage() {
        assertTrue(mlService.validateDeliveryImage("https://example.com/clear_photo.jpg"));
        assertTrue(mlService.validateDeliveryImage("good_dropoff.png"));
    }

    @Test
    public void testBlurryImage() {
        assertFalse(mlService.validateDeliveryImage("https://example.com/blurry_photo.jpg"));
        assertFalse(mlService.validateDeliveryImage("blur.png"));
    }

    @Test
    public void testInvalidImage() {
        assertFalse(mlService.validateDeliveryImage("invalid_format.txt"));
        assertFalse(mlService.validateDeliveryImage("dark_doorstep.jpg"));
    }
    
    @Test
    public void testNullOrEmpty() {
        assertFalse(mlService.validateDeliveryImage(null));
        assertFalse(mlService.validateDeliveryImage("  "));
    }
}
