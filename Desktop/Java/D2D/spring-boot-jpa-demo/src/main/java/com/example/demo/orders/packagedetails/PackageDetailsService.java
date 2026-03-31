package com.example.demo.orders.packagedetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PackageDetailsService {

    private final PackageDetailsRepository repository;

    @Autowired
    public PackageDetailsService(PackageDetailsRepository repository) {
        this.repository = repository;
    }

    public PackageDetailsDTO createPackageDetails(PackageDetailsDTO dto) {
        PackageDetails entity = mapToEntity(dto);
        PackageDetails savedEntity = repository.save(entity);
        return mapToDTO(savedEntity);
    }

    public List<PackageDetailsDTO> getAllPackageDetails() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PackageDetailsDTO> getPackageDetailsById(Long id) {
        return repository.findById(id).map(this::mapToDTO);
    }

    public void deletePackageDetails(Long id) {
        repository.deleteById(id);
    }

    private PackageDetails mapToEntity(PackageDetailsDTO dto) {
        return PackageDetails.builder()
                .id(dto.getId())
                .packageCount(dto.getPackageCount())
                .boxId(dto.getBoxId())
                .boxType(dto.getBoxType())
                .lengthCm(dto.getLengthCm())
                .widthCm(dto.getWidthCm())
                .heightCm(dto.getHeightCm())
                .weightKg(dto.getWeightKg())
                .fragile(dto.isFragile())
                .hazardousMaterial(dto.isHazardousMaterial())
                .packagingType(dto.getPackagingType())
                .sealType(dto.getSealType())
                .handlingInstructions(dto.getHandlingInstructions())
                .build();
    }

    private PackageDetailsDTO mapToDTO(PackageDetails entity) {
        return PackageDetailsDTO.builder()
                .id(entity.getId())
                .packageCount(entity.getPackageCount())
                .boxId(entity.getBoxId())
                .boxType(entity.getBoxType())
                .lengthCm(entity.getLengthCm())
                .widthCm(entity.getWidthCm())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .fragile(entity.isFragile())
                .hazardousMaterial(entity.isHazardousMaterial())
                .packagingType(entity.getPackagingType())
                .sealType(entity.getSealType())
                .handlingInstructions(entity.getHandlingInstructions())
                .build();
    }
}
