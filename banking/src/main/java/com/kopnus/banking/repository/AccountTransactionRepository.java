package com.kopnus.banking.repository;

import com.kopnus.banking.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID> {
}
