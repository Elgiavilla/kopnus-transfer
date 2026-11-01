package com.kopnus.banking.controller;

import com.kopnus.banking.models.request.TransferRequest;
import com.kopnus.banking.models.response.TransferOutcome;
import com.kopnus.banking.models.response.TransferResponse;
import com.kopnus.banking.service.TransferService;
import com.kopnus.banking.shared.base.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransferOutcome outcome = transferService.transfer(userId, idempotencyKey, request);

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (outcome.isReplayed()) {
            builder.header("Idempotent-Replayed", "true");
        }
        return builder.body(outcome.getResponse());
    }
}
