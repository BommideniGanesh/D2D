package com.example.demo.orders.senderdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SenderDetailsService {

    private final SenderDetailsRepository repository;

    @Autowired
    public SenderDetailsService(SenderDetailsRepository repository) {
        this.repository = repository;
    }

    public SenderDetails createSender(SenderDetails senderDetails) {
        return repository.save(senderDetails);
    }

    public List<SenderDetails> getAllSenders() {
        return repository.findAll();
    }

    public Optional<SenderDetails> getSenderById(String id) {
        return repository.findById(id);
    }

    public SenderDetails updateSender(String id, SenderDetails senderDetails) {
        if (repository.existsById(id)) {
            senderDetails.setId(id);
            return repository.save(senderDetails);
        }
        return null;
    }

    public void deleteSender(String id) {
        repository.deleteById(id);
    }
}
