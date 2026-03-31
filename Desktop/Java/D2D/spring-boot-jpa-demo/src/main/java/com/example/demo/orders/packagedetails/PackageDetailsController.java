package com.example.demo.orders.packagedetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class PackageDetailsController {

    private final PackageDetailsService service;

    @Autowired
    public PackageDetailsController(PackageDetailsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PackageDetailsDTO> createPackageDetails(@RequestBody PackageDetailsDTO dto) {
        PackageDetailsDTO created = service.createPackageDetails(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PackageDetailsDTO>> getAllPackageDetails() {
        List<PackageDetailsDTO> packages = service.getAllPackageDetails();
        return new ResponseEntity<>(packages, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageDetailsDTO> getPackageDetailsById(@PathVariable Long id) {
        return service.getPackageDetailsById(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackageDetails(@PathVariable Long id) {
        service.deletePackageDetails(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
