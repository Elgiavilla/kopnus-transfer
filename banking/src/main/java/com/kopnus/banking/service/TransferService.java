package com.kopnus.banking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopnus.banking.config.exception.BusinessException;
import com.kopnus.banking.entity.*;
import com.kopnus.banking.models.request.TransferRequest;
import com.kopnus.banking.models.response.TransferOutcome;
import com.kopnus.banking.models.response.TransferResponse;
import com.kopnus.banking.repository.AccountRepository;
import com.kopnus.banking.repository.AccountTransactionRepository;
import com.kopnus.banking.repository.IdempotencyKeyRepository;
import com.kopnus.banking.repository.TransferRepository;
import com.kopnus.banking.shared.constant.ResponseCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransferOutcome transfer(UUID userId, String idempotencyKey, TransferRequest req) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Idempotency-Key wajib diisi dan maksimal " + MAX_IDEMPOTENCY_KEY_LENGTH + " karakter");
        }
        String fromNumber = req.getFromAccountNumber().trim();
        String toNumber = req.getToAccountNumber().trim();
        if (fromNumber.equals(toNumber)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Rekening asal dan tujuan tidak boleh sama");
        }
        BigDecimal amount = req.getAmount().setScale(2, RoundingMode.UNNECESSARY);
        String note = req.getNote() == null ? "" : req.getNote().trim();

        String requestHash = sha256Hex(fromNumber + "|" + toNumber + "|" + amount.toPlainString() + "|" + note);
        int inserted = idempotencyKeyRepository.insertIfAbsent(userId, idempotencyKey, requestHash);
        IdempotencyKey idem = idempotencyKeyRepository.findByUserIdAndKey(userId, idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Idempotency key tidak ditemukan setelah insert"));
        if (inserted == 0) {
            return replay(idem, requestHash);
        }

        Optional<UUID> fromId = accountRepository.findIdByAccountNumber(fromNumber);
        if(!fromId.isPresent()){
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Account asal tidak ditemukan");
        }
        Optional<UUID> toId = accountRepository.findIdByAccountNumber(toNumber);
        if(!toId.isPresent()){
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Account tujuan tidak ditemukan");
        }

        boolean fromFirst = fromId.get().compareTo(toId.get()) < 0;
        Account first = lockAccount(fromFirst ? fromId.get() : toId.get());
        Account second = lockAccount(fromFirst ? toId.get() : fromId.get());
        Account source = fromFirst ? first : second;
        Account destination = fromFirst ? second : first;

        if (!source.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "Rekening asal bukan milik Anda");
        }
        BigDecimal sourceBefore = source.getBalance();
        BigDecimal destinationBefore = destination.getBalance();
        if (sourceBefore.compareTo(amount) < 0) {
            throw new BusinessException(ResponseCode.UNPROCESSABLE_ENTITY, "Saldo tidak mencukupi");
        }


        BigDecimal sourceAfter = sourceBefore.subtract(amount);
        BigDecimal destinationAfter = destinationBefore.add(amount);
        source.setBalance(sourceAfter);
        destination.setBalance(destinationAfter);

        Transfer transfer = new Transfer();
        transfer.setUserId(userId);
        transfer.setIdempotencyKeyId(idem.getId());
        transfer.setFromAccountId(source.getId());
        transfer.setToAccountId(destination.getId());
        transfer.setAmount(amount);
        transfer.setNote(note.isEmpty() ? null : note);
        transfer.setStatus(TransferStatus.SUCCESS);
        transfer = transferRepository.save(transfer);


        accountTransactionRepository.save(ledger(source.getId(), transfer.getId(), TransactionType.DEBIT,
                TransactionCategory.TRANSFER_OUT, amount, sourceBefore, sourceAfter, "Transfer ke " + toNumber));
        accountTransactionRepository.save(ledger(destination.getId(), transfer.getId(), TransactionType.CREDIT,
                TransactionCategory.TRANSFER_IN, amount, destinationBefore, destinationAfter, "Transfer dari " + fromNumber));


        TransferResponse response = new TransferResponse(transfer.getId(), transfer.getStatus().name(),
                fromNumber, toNumber, amount, transfer.getNote(), sourceAfter, transfer.getCreatedAt());
        idem.setResponseStatus(HttpStatus.OK.value());
        idem.setResponseBody(toJson(response));

        return new TransferOutcome(response, false);
    }

    private TransferOutcome replay(IdempotencyKey idem, String requestHash) {
        if (!idem.getRequestHash().equals(requestHash)) {
            throw new BusinessException(ResponseCode.UNPROCESSABLE_ENTITY,
                    "Idempotency-Key ini sudah dipakai untuk request dengan data berbeda");
        }
        if (idem.getResponseBody() == null) {
            throw new BusinessException(ResponseCode.INSUFFICIENT_FUNC, "Request sedang diproses");
        }
        try {
            return new TransferOutcome(objectMapper.readValue(idem.getResponseBody(), TransferResponse.class), true);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gagal membaca response idempotent", e);
        }
    }

    private AccountTransaction ledger(UUID accountId, UUID transferId, TransactionType type,
                                      TransactionCategory category, BigDecimal amount,
                                      BigDecimal before, BigDecimal after, String description) {
        AccountTransaction tx = new AccountTransaction();
        tx.setAccountId(accountId);
        tx.setTransferId(transferId);
        tx.setType(type);
        tx.setCategory(category);
        tx.setAmount(amount);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setDescription(description);
        return tx;
    }


    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gagal membuat JSON response", e);
        }
    }

    private Account lockAccount(UUID id) {
        return accountRepository.findByIdForUpdate(id).get();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
