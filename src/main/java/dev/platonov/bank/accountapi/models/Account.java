package dev.platonov.bank.accountapi.models;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class Account {
    private Long id;
    private @NonNull BigDecimal amount;
    private boolean active;
    private final @NonNull LocalDateTime createdAt;
    private @NonNull LocalDateTime updatedAt;

    public Account() {
        this(null, BigDecimal.ZERO);
    }

    public Account(long id) {
        this(id, BigDecimal.ZERO);
    }

    public Account(@NonNull BigDecimal amount) {
        this(null, amount);
    }

    public Account(Long id, @NonNull BigDecimal amount) {
        this(id, amount, true, null, null);
    }

    @ConstructorProperties({
            "id",
            "amount",
            "active",
            "created_at",
            "updated_at"
    })
    public Account(
            long id,
            @NonNull BigDecimal amount,
            boolean active,
            @NonNull Timestamp createdAt,
            @NonNull Timestamp updatedAt
    ) {
        this(id, amount, active, createdAt.toLocalDateTime(), updatedAt.toLocalDateTime());
    }

    @Builder
    public Account(Long id, @NonNull BigDecimal amount, boolean active,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.amount = amount;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }
}