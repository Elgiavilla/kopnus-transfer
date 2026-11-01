package com.kopnus.banking.controller;

import com.kopnus.banking.models.request.LoginRequest;
import com.kopnus.banking.models.request.RefreshRequest;
import com.kopnus.banking.models.request.RegisterRequest;
import com.kopnus.banking.models.response.RegisterResponse;
import com.kopnus.banking.models.response.TokenResponse;
import com.kopnus.banking.service.AuthService;
import com.kopnus.banking.shared.base.BaseResponse;
import com.kopnus.banking.shared.base.CommonResponse;
import com.kopnus.banking.shared.constant.ResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return CommonResponse.constructResponse(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                null,
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return CommonResponse.constructResponse(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                null,
                authService.login(request)
        );
    }

    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return CommonResponse.constructResponse(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                null,
                authService.refresh(request)
        );
    }
}
