package com.kopnus.banking.service;

import com.kopnus.banking.config.exception.BusinessException;
import com.kopnus.banking.config.security.JwtProperties;
import com.kopnus.banking.entity.Account;
import com.kopnus.banking.entity.RefreshToken;
import com.kopnus.banking.entity.User;
import com.kopnus.banking.models.request.LoginRequest;
import com.kopnus.banking.models.request.RefreshRequest;
import com.kopnus.banking.models.request.RegisterRequest;
import com.kopnus.banking.models.response.RegisterResponse;
import com.kopnus.banking.models.response.TokenResponse;
import com.kopnus.banking.repository.AccountRepository;
import com.kopnus.banking.repository.RefreshTokenRepository;
import com.kopnus.banking.repository.UserRepository;
import com.kopnus.banking.shared.constant.ResponseCode;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties props;

    private final SecureRandom random = new SecureRandom();
    private String dummyHash;

    @PostConstruct
    void init() {
                dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        String username = normalize(req.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Username telah tersedia, pakai username yang lain");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Username telah tersedia, pakai username yang lain");
        }


        Account account = new Account();
        account.setUserId(user.getId());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO.setScale(2));
        accountRepository.save(account);

        return new RegisterResponse(user.getId(), user.getUsername(), account.getAccountNumber());
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(normalize(req.getUsername())).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(req.getPassword(), hash);
        if (user == null || !matches) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, ResponseCode.UNAUTHORIZED.getMessage());
        }
        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(sha256Hex(req.getRefreshToken()))
                .orElseThrow(() -> new BusinessException(ResponseCode.UNAUTHORIZED, ResponseCode.UNAUTHORIZED.getMessage()));
        Instant now = Instant.now();

        if (stored.getRevokedAt() != null) {
            // Token yang sudah dirotasi dipakai lagi: kemungkinan dicuri, cabut semua sesi user
            refreshTokenRepository.revokeAllByUserId(stored.getUserId(), now);
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Refresh token sudah digunakan, silakan login ulang");
        }
        if (stored.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Refresh token kedaluwarsa, silakan login ulang");
        }

        Optional<User> user = userRepository.findById(stored.getUserId());
        if(user.isPresent()){
            throw new BusinessException(ResponseCode.BAD_REQUEST, "Refresh token tidak valid");
        }

        stored.setRevokedAt(now); // rotasi: token lama langsung dicabut
        return issueTokens(user.get());
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawRefresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256Hex(rawRefresh)); // yang disimpan hanya hash-nya
        rt.setExpiresAt(Instant.now().plus(props.refreshTtlDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);

        return new TokenResponse(accessToken, rawRefresh, "Bearer", props.accessTtlMinutes() * 60);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "10" + String.format("%08d", random.nextInt(100_000_000));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
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
