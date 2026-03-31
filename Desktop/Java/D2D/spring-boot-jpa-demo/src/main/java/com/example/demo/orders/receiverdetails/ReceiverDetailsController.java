package com.example.demo.orders.receiverdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receivers")
public class ReceiverDetailsController {

    private final ReceiverDetailsService service;

    @Autowired
    public ReceiverDetailsController(ReceiverDetailsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReceiverDetailsDTO> createReceiver(@RequestBody ReceiverDetailsDTO dto) {
        ReceiverDetailsDTO createdReceiver = service.createReceiver(dto);
        return new ResponseEntity<>(createdReceiver, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReceiverDetailsDTO>> getAllReceivers() {
        List<ReceiverDetailsDTO> receivers = service.getAllReceivers();
        return new ResponseEntity<>(receivers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiverDetailsDTO> getReceiverById(@PathVariable String id) {
        return service.getReceiverById(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceiverDetailsDTO> updateReceiver(@PathVariable String id,
            @RequestBody ReceiverDetailsDTO dto) {
        ReceiverDetailsDTO updatedReceiver = service.updateReceiver(id, dto);
        if (updatedReceiver != null) {
            return new ResponseEntity<>(updatedReceiver, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReceiver(@PathVariable String id) {
        service.deleteReceiver(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
