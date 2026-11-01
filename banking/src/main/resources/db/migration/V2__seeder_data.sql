-- V2: demo data (password semua user: password123)
-- pgcrypto menghasilkan hash BCrypt ($2a$) yang kompatibel dengan Spring BCryptPasswordEncoder
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (username, password_hash) VALUES
                                                ('alice', crypt('password123', gen_salt('bf', 10))),
                                                ('bob',   crypt('password123', gen_salt('bf', 10))),
                                                ('carol', crypt('password123', gen_salt('bf', 10)));

INSERT INTO accounts (user_id, account_number, balance)
SELECT u.id, v.account_number, v.balance
FROM (VALUES
          ('alice', '1000000001', 10000000.00),
          ('bob',   '1000000002',  5000000.00),
          ('carol', '1000000003',  2500000.00)
     ) AS v (username, account_number, balance)
         JOIN users u ON u.username = v.username;

INSERT INTO account_transactions
(account_id, transfer_id, type, category, amount, balance_before, balance_after, description)
SELECT a.id, NULL, 'CREDIT', 'OPENING_BALANCE', a.balance, 0, a.balance, 'Opening balance'
FROM accounts a;