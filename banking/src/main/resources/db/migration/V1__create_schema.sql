CREATE TABLE users (
   id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
   username      VARCHAR(50)  NOT NULL UNIQUE,
   password_hash VARCHAR(100) NOT NULL,
   created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
  id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID           NOT NULL REFERENCES users (id),
  account_number VARCHAR(20)    NOT NULL UNIQUE,
  balance        NUMERIC(19, 2) NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_accounts_user_id ON accounts (user_id);

CREATE TABLE idempotency_keys (
  id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID         NOT NULL REFERENCES users (id),
  idempotency_key VARCHAR(100) NOT NULL,
  request_hash    VARCHAR(64)  NOT NULL,
  response_status INT,
  response_body   JSONB,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uq_idempotency_user_key UNIQUE (user_id, idempotency_key)
);

CREATE TABLE transfers (
    id                 UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID           NOT NULL REFERENCES users (id),
    idempotency_key_id UUID           NOT NULL REFERENCES idempotency_keys (id),
    from_account_id    UUID           NOT NULL REFERENCES accounts (id),
    to_account_id      UUID           NOT NULL REFERENCES accounts (id),
    amount             NUMERIC(19, 2) NOT NULL,
    note               VARCHAR(255),
    status             VARCHAR(20)    NOT NULL,
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_transfers_idempotency_key UNIQUE (idempotency_key_id)
);
CREATE INDEX idx_transfers_from_account ON transfers (from_account_id, created_at DESC);
CREATE INDEX idx_transfers_to_account ON transfers (to_account_id, created_at DESC);

CREATE TABLE account_transactions (
      id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
      account_id     UUID           NOT NULL REFERENCES accounts (id),
      transfer_id    UUID           REFERENCES transfers (id),
      type           VARCHAR(10)    NOT NULL,
      category       VARCHAR(20)    NOT NULL,
      amount         NUMERIC(19, 2) NOT NULL,
      balance_before NUMERIC(19, 2) NOT NULL,
      balance_after  NUMERIC(19, 2) NOT NULL,
      description    VARCHAR(255),
      created_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);
CREATE INDEX idx_account_tx_account_created ON account_transactions (account_id, created_at DESC);
CREATE INDEX idx_account_tx_transfer ON account_transactions (transfer_id);

CREATE TABLE refresh_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);