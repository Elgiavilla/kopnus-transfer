package com.kopnus.banking.models.response;

import lombok.Data;

import java.util.UUID;

@Data
public class RegisterResponse {

    private UUID userId;
    private String username;
    private String accountNumber;

    public RegisterResponse(UUID userId, String username, String accountNumber){
        this.userId = userId;
        this.username = username;
        this.accountNumber = accountNumber;
    }

}
