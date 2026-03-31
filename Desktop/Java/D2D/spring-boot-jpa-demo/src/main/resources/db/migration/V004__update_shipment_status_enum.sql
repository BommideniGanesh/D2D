ALTER TABLE shipments
MODIFY COLUMN status ENUM(
        'CREATED',
        'PICKUP_ASSIGNED',
        'PICKED_UP',
        'IN_TRANSIT',
        'OUT_FOR_DELIVERY',
        'DELIVERED',
        'CANCELLED'
    ) NOT NULL;