package dev.platonov.bank.accountapi.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class ReserveOption {
    @NonNull String id;
    long accountId;
    @NonNull ReserveType reserveType;
    @NonNull BigDecimal amount;
}
