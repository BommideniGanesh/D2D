package com.example.demo.notifications.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhookDispatchServiceTest {

    @Mock
    private WebhookDeliveryAttemptRepository attemptRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WebhookDispatchService dispatchService;

    private WebhookSubscription subscription;
    private final String PAYLOAD = "{\"status\": \"DELIVERED\"}";

    @BeforeEach
    void setUp() {
        // Inject our mocked RestTemplate because the Service originally instantiates a new one
        ReflectionTestUtils.setField(dispatchService, "restTemplate", restTemplate);

        subscription = WebhookSubscription.builder()
                .id(100L)
                .userId("client-a")
                .targetUrl("https://api.client-a.com/webhooks")
                .secretKey("my-super-secret-key")
                .isActive(true)
                .build();
    }

    @Test
    void testDispatch_Success() {
        when(restTemplate.postForEntity(eq(subscription.getTargetUrl()), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        dispatchService.dispatch(subscription, "SHIPMENT.STATUS.UPDATED", PAYLOAD);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(subscription.getTargetUrl()), entityCaptor.capture(), eq(String.class));

        HttpEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getHeaders().get("X-Webhook-Signature"));
        assertNotNull(capturedEntity.getHeaders().get("X-Event-Type"));
        assertEquals("SHIPMENT.STATUS.UPDATED", capturedEntity.getHeaders().getFirst("X-Event-Type"));

        ArgumentCaptor<WebhookDeliveryAttempt> attemptCaptor = ArgumentCaptor.forClass(WebhookDeliveryAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());

        WebhookDeliveryAttempt savedAttempt = attemptCaptor.getValue();
        assertTrue(savedAttempt.isSuccessful());
        assertEquals(200, savedAttempt.getResponseStatusCode());
        assertEquals(PAYLOAD, savedAttempt.getPayload());
    }

    @Test
    void testDispatch_Failure() {
        when(restTemplate.postForEntity(eq(subscription.getTargetUrl()), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection Refused"));

        dispatchService.dispatch(subscription, "SHIPMENT.STATUS.UPDATED", PAYLOAD);

        ArgumentCaptor<WebhookDeliveryAttempt> attemptCaptor = ArgumentCaptor.forClass(WebhookDeliveryAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());

        WebhookDeliveryAttempt savedAttempt = attemptCaptor.getValue();
        assertFalse(savedAttempt.isSuccessful());
        assertNull(savedAttempt.getResponseStatusCode());
    }
}
