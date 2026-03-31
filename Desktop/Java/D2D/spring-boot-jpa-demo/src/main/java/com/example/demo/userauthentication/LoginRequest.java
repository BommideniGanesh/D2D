package com.example.demo.userauthentication;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
