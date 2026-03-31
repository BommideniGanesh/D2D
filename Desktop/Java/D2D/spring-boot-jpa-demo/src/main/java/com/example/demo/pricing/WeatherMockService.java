package com.example.demo.pricing;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class WeatherMockService {
    private final Random random = new Random();
    
    /**
     * Mocks a call to an external Weather API to get the current condition at the origin pincode.
     */
    public String getCurrentWeather(String postalCode) {
        // In a real app, we'd use the postal code to call OpenWeatherMap or similar.
        String[] conditions = {"CLEAR", "RAIN", "SNOW", "STORM"};
        return conditions[random.nextInt(conditions.length)];
    }
}
