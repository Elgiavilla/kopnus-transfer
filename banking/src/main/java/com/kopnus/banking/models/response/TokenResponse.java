package com.kopnus.banking.models.response;

import lombok.Data;

@Data
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

    public TokenResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn){
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

}
