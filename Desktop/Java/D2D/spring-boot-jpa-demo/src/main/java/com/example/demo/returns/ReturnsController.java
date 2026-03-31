package com.example.demo.returns;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/returns")
public class ReturnsController {

    private final ReturnService returnService;

    public ReturnsController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    public ResponseEntity<?> createReturnRequest(@RequestBody Map<String, Object> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser"))
                    ? authentication.getName() : "testUser";

            Long shipmentId = Long.valueOf(payload.get("shipmentId").toString());
            String reason = payload.get("reason").toString();

            ReturnRequest created = returnService.requestReturn(shipmentId, reason, requestedBy);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "NullPointerException"));
        }
    }
}
