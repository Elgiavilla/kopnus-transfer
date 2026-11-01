package com.kopnus.banking.models.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequest {

    @NotBlank
    @Size(max = 20)
    private String fromAccountNumber;

    @NotBlank
    @Size(max = 20)
    private String toAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01", message = "amount minimal 0.01")
    @Digits(integer = 17, fraction = 2, message = "amount maksimal 2 angka di belakang koma")
    private BigDecimal amount;

    @Size(max = 255)
    private String note;
}
