package com.example.demo.orders.receiverdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiverDetailsDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private AddressDTO address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String deliveryInstructions;
        private boolean isResidential;
    }
}
