package com.example.demo.userregistration;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private boolean acceptedTerms;
    private String role;
}
