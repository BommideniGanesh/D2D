package com.example.demo.notifications.webhook;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class WebhookDispatchService {

    private final WebhookDeliveryAttemptRepository attemptRepository;
    private final RestTemplate restTemplate;

    public WebhookDispatchService(WebhookDeliveryAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
        this.restTemplate = new RestTemplate(); // For production, inject a configured RestTemplate bean
    }

    public void dispatch(WebhookSubscription subscription, String eventType, String jsonPayload) {
        WebhookDeliveryAttempt attempt = WebhookDeliveryAttempt.builder()
                .subscription(subscription)
                .eventType(eventType)
                .payload(jsonPayload)
                .attemptCount(1)
                .build();

        try {
            // Generate HMAC Signature
            String signature = calculateHmacSha256(jsonPayload, subscription.getSecretKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", signature);
            headers.set("X-Event-Type", eventType);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            // Execute POST
            ResponseEntity<String> response = restTemplate.postForEntity(subscription.getTargetUrl(), request, String.class);
            
            attempt.setResponseStatusCode(response.getStatusCode().value());
            attempt.setSuccessful(response.getStatusCode().is2xxSuccessful());

        } catch (Exception e) {
            // Delivery failed (Network error, timeout, 500 etc)
            attempt.setSuccessful(false);
            // We could optionally log the exception message to the DB here
        } finally {
            attemptRepository.save(attempt);
        }
    }

    private String calculateHmacSha256(String data, String key) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
