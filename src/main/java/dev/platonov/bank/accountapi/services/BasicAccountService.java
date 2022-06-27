package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.repositories.AccountRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class BasicAccountService implements AccountService {

    private final AccountRepository repository;

    public BasicAccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public @NonNull Account create() {
        return repository.save(
                Account.builder()
                        .amount(BigDecimal.ZERO)
                        .build()
        );
    }

    @Override
    public @NonNull Account incr(long id, @NonNull BigDecimal amount) {
        throwIfNotExists(id);
        return repository.incr(id, amount);
    }

    @Override
    public @NonNull Account decr(long id, @NonNull BigDecimal amount) {
        throwIfNotExists(id);
        return repository.decr(id, amount);
    }

    @Override
    public void close(@NonNull Account account) {
        repository.close(account);
    }

    @Override
    public @NonNull Optional<Account> getActive(long id) {
        return repository.getActive(id);
    }

    @Override
    public @NonNull Account getActiveUnsafe(long id) {
        return getActive(id)
                .orElseThrow(() -> {
                    throw new IllegalArgumentException(
                            String.format("unknown account with id = %d", id)
                    );
                });
    }

    @Override
    public boolean existsActive(long id) {
        return repository.existsAndActive(id);
    }

    @Override
    public void throwIfNotExists(long id) {
        if (!existsActive(id)) {
            throw new IllegalArgumentException(
                    String.format("unknown account with id = %d", id)
            );
        }
    }
}
