package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.models.Account;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository {
    @NonNull Account save(@NonNull Account account);

    void update(@NonNull Account account);

    @NonNull Account incr(long id, @NonNull BigDecimal amount);

    @NonNull Account decr(long id, @NonNull BigDecimal amount);

    void close(@NonNull Account account);

    @NonNull Optional<Account> get(long id);

    @NonNull Optional<Account> getActive(long id);

    boolean existsAndActive(long id);
}
