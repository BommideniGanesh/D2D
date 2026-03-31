package com.example.demo.userregistration;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String email;
    private String phone;
}
