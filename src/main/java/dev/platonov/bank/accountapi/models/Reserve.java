package dev.platonov.bank.accountapi.models;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class Reserve {
    private final @NonNull String id; // freestyle id allowed to generate in a remote system
    private final long accountId;
    private final @NonNull BigDecimal amount;
    private final @NonNull ReserveType type;
    private final @NonNull LocalDateTime createdAt;

    @Builder
    public Reserve(
            @NonNull String id,
            long accountId,
            @NonNull BigDecimal amount,
            @NonNull ReserveType type,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @ConstructorProperties({"id", "account_id", "amount", "type", "created_at"})
    public Reserve(
            @NonNull String id,
            long accountId,
            @NonNull BigDecimal amount,
            @NonNull ReserveType type,
            @NonNull Timestamp createdAt
    ) {
        this(id, accountId, amount, type, createdAt.toLocalDateTime());
    }

    public static Reserve deposit(@NonNull String id, long accountId, @NonNull BigDecimal amount) {
        return Reserve.builder()
                .id(id)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .amount(amount)
                .type(ReserveType.DEPOSIT)
                .build();
    }

    public static Reserve withdraw(@NonNull String id, long accountId, @NonNull BigDecimal amount) {
        return Reserve.builder()
                .id(id)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .amount(amount)
                .type(ReserveType.WITHDRAW)
                .build();
    }

}
