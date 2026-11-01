package com.kopnus.banking.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest{

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "hanya huruf, angka, titik, underscore, dan strip")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

}
