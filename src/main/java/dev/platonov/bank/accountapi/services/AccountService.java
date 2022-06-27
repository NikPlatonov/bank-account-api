package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.models.Account;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService { // TODO: 28.05.2022 add throws
    @NonNull Account create();

    @NonNull Account incr(long id, @NonNull BigDecimal amount);

    @NonNull Account decr(long id, @NonNull BigDecimal amount);

    void close(@NonNull Account account);

    @NonNull Account getActiveUnsafe(long id); // throws IllegalArgumentException if account with id doesn't exist
    @NonNull Optional<Account> getActive(long id);

    boolean existsActive(long id);

    void throwIfNotExists(long id);
}
