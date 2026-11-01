package com.kopnus.banking.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private UUID transferId;
    private String status;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String note;
    private BigDecimal senderBalanceAfter;
    private Instant createdAt;
}
