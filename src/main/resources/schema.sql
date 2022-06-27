create table IF NOT EXISTS accounts
(
    id         bigserial,
    amount     numeric,
    active     boolean,
    created_at timestamp with time zone,
    updated_at timestamp with time zone,
    primary key (id)
);

create table IF NOT EXISTS reserves
(
    id         text,
    account_id bigint,
    amount     numeric,
    type       text,
    created_at timestamp with time zone,
    primary key (id),
    CONSTRAINT fk_customer
      FOREIGN KEY (account_id)
        REFERENCES accounts (id)
);

ALTER TABLE reserves
    SET (autovacuum_enabled = true);

