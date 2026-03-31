package com.example.demo.orders.senderdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/senders")
public class SenderDetailsController {

    private final SenderDetailsService service;

    @Autowired
    public SenderDetailsController(SenderDetailsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SenderDetails> createSender(@RequestBody SenderDetails senderDetails) {
        SenderDetails createdSender = service.createSender(senderDetails);
        return new ResponseEntity<>(createdSender, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SenderDetails>> getAllSenders() {
        List<SenderDetails> senders = service.getAllSenders();
        return new ResponseEntity<>(senders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SenderDetails> getSenderById(@PathVariable String id) {
        return service.getSenderById(id)
                .map(sender -> new ResponseEntity<>(sender, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SenderDetails> updateSender(@PathVariable String id,
            @RequestBody SenderDetails senderDetails) {
        SenderDetails updatedSender = service.updateSender(id, senderDetails);
        if (updatedSender != null) {
            return new ResponseEntity<>(updatedSender, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSender(@PathVariable String id) {
        service.deleteSender(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
