package com.kopnus.banking.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferOutcome {

    private TransferResponse response;
    private boolean replayed;
}
