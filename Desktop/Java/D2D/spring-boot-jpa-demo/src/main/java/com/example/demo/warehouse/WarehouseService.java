package com.example.demo.warehouse;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.orders.shipment.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final TransitBagRepository transitBagRepository;
    private final ShipmentRepository shipmentRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            TransitBagRepository transitBagRepository,
                            ShipmentRepository shipmentRepository) {
        this.warehouseRepository = warehouseRepository;
        this.transitBagRepository = transitBagRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public TransitBag openTransitBag(String originCode, String destCode) {
        Warehouse origin = warehouseRepository.findByLocationCode(originCode)
                .orElseThrow(() -> new RuntimeException("Origin Hub not found"));
        Warehouse dest = warehouseRepository.findByLocationCode(destCode)
                .orElseThrow(() -> new RuntimeException("Destination Hub not found"));

        TransitBag bag = TransitBag.builder()
                .bagBarcode("BAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .originWarehouse(origin)
                .destinationWarehouse(dest)
                .status(TransitBag.BagStatus.OPEN)
                .build();

        return transitBagRepository.save(bag);
    }

    @Transactional
    public void scanShipmentIntoBag(String trackingNumber, String bagBarcode) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        TransitBag bag = transitBagRepository.findByBagBarcode(bagBarcode)
                .orElseThrow(() -> new RuntimeException("Bag not found"));

        if (bag.getStatus() != TransitBag.BagStatus.OPEN) {
            throw new RuntimeException("Cannot add shipment; Bag is no longer OPEN.");
        }

        shipment.setTransitBag(bag);
        shipment.setStatus(Shipment.ShipmentStatus.BAGGED);
        shipmentRepository.save(shipment);
    }

    @Transactional
    public void sealBagForTransit(String bagBarcode) {
        TransitBag bag = transitBagRepository.findByBagBarcode(bagBarcode)
                .orElseThrow(() -> new RuntimeException("Bag not found"));
        
        bag.setStatus(TransitBag.BagStatus.SEALED);
        bag.setSealedAt(new Timestamp(System.currentTimeMillis()));
        transitBagRepository.save(bag);

        // Update all shipments to IN_TRANSIT
        for (Shipment s : bag.getShipments()) {
            s.setStatus(Shipment.ShipmentStatus.IN_TRANSIT);
            shipmentRepository.save(s);
        }
    }

    @Transactional
    public void receiveAndUnpackBag(String bagBarcode, String currentLocationCode) {
        TransitBag bag = transitBagRepository.findByBagBarcode(bagBarcode)
                .orElseThrow(() -> new RuntimeException("Bag not found"));
        
        Warehouse currentHub = warehouseRepository.findByLocationCode(currentLocationCode)
                .orElseThrow(() -> new RuntimeException("Hub not found"));

        bag.setStatus(TransitBag.BagStatus.UNPACKED);
        transitBagRepository.save(bag);

        // Unpack individual shipments, allowing final mile allocation
        for (Shipment s : bag.getShipments()) {
            s.setTransitBag(null); // Detach from bag
            s.setStatus(Shipment.ShipmentStatus.AT_HUB);
            shipmentRepository.save(s);
        }
    }
}
