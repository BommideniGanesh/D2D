package com.example.demo.orders.receiverdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReceiverDetailsService {

    private final ReceiverDetailsRepository repository;

    @Autowired
    public ReceiverDetailsService(ReceiverDetailsRepository repository) {
        this.repository = repository;
    }

    public ReceiverDetailsDTO createReceiver(ReceiverDetailsDTO dto) {
        ReceiverDetails entity = mapToEntity(dto);
        ReceiverDetails savedEntity = repository.save(entity);
        return mapToDTO(savedEntity);
    }

    public List<ReceiverDetailsDTO> getAllReceivers() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ReceiverDetailsDTO> getReceiverById(String id) {
        return repository.findById(id).map(this::mapToDTO);
    }

    public ReceiverDetailsDTO updateReceiver(String id, ReceiverDetailsDTO dto) {
        if (repository.existsById(id)) {
            ReceiverDetails entity = mapToEntity(dto);
            entity.setId(id);
            // Ensure address is properly set if it's null in DTO but present in DB?
            // Simplified update: overwrite everything.
            ReceiverDetails updatedEntity = repository.save(entity);
            return mapToDTO(updatedEntity);
        }
        return null;
    }

    public void deleteReceiver(String id) {
        repository.deleteById(id);
    }

    private ReceiverDetails mapToEntity(ReceiverDetailsDTO dto) {
        return ReceiverDetails.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .address(mapAddressToEntity(dto.getAddress()))
                .build();
    }

    private Address mapAddressToEntity(ReceiverDetailsDTO.AddressDTO dto) {
        if (dto == null)
            return null;
        return Address.builder()
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .deliveryInstructions(dto.getDeliveryInstructions())
                .isResidential(dto.isResidential())
                .build();
    }

    private ReceiverDetailsDTO mapToDTO(ReceiverDetails entity) {
        return ReceiverDetailsDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .address(mapAddressToDTO(entity.getAddress()))
                .build();
    }

    private ReceiverDetailsDTO.AddressDTO mapAddressToDTO(Address entity) {
        if (entity == null)
            return null;
        return ReceiverDetailsDTO.AddressDTO.builder()
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .deliveryInstructions(entity.getDeliveryInstructions())
                .isResidential(entity.isResidential())
                .build();
    }
}
