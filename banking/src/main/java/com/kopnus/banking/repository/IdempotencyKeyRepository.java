package com.kopnus.banking.repository;

import com.kopnus.banking.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {


    @Modifying
    @Query(value = "INSERT INTO idempotency_keys (id, user_id, idempotency_key, request_hash, created_at) "
            + "VALUES (gen_random_uuid(), :userId, :key, :hash, now()) "
            + "ON CONFLICT (user_id, idempotency_key) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("userId") UUID userId, @Param("key") String key, @Param("hash") String hash);

    Optional<IdempotencyKey> findByUserIdAndKey(UUID userId, String key);
}
