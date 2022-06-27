package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.exceptions.AlreadyHandledReserveException;
import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.exceptions.NotUniqueIdException;
import dev.platonov.bank.accountapi.models.*;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BasicAccountManager implements AccountManager {
    private final AccountService accountService;
    private final ReserveService reserveService;

    public BasicAccountManager(
            AccountService accountService,
            ReserveService reserveService
    ) {
        this.accountService = accountService;
        this.reserveService = reserveService;
    }

    @Override
    public @NonNull Optional<Account> getActiveAccount(long id) {
        return accountService.getActive(id);
    }

    @Override
    public @NonNull Optional<Reserve> getReserve(@NonNull String id) {
        return reserveService.get(id);
    }

    @Transactional
    @Override
    public @NonNull Reserve reserve(@NonNull ReserveOption reserveOption) throws DeniedReserveException {
        accountService.throwIfNotExists(reserveOption.getAccountId());

        var reserve = Reserve.builder()
                .id(reserveOption.getId())
                .accountId(reserveOption.getAccountId())
                .amount(reserveOption.getAmount())
                .createdAt(LocalDateTime.now())
                .type(reserveOption.getReserveType())
                .build();

        throwIfInvalid(reserve);

        reserveService.saveIfAllowed(reserve);

        return reserve;
    }

    @Transactional
    @Override
    public @NonNull Reserve reserveDeposit(@NonNull String id, long accountId,
                                           @NonNull BigDecimal amount) throws DeniedReserveException {
        var option = ReserveOption.builder()
                .reserveType(ReserveType.DEPOSIT)
                .accountId(accountId)
                .id(id)
                .amount(amount)
                .build();
        return reserve(option);
    }

    @Transactional
    @Override
    public @NonNull Reserve reserveWithdraw(@NonNull String id, long accountId,
                                            @NonNull BigDecimal amount) throws DeniedReserveException {
        var option = ReserveOption.builder()
                .reserveType(ReserveType.WITHDRAW)
                .accountId(accountId)
                .id(id)
                .amount(amount)
                .build();
        return reserve(option);
    }

    private void throwIfInvalid(@NonNull Reserve reserve) {
        if (reserveService.exists(reserve.getId())) {
            throw NotUniqueIdException.builder()
                    .model("reserve")
                    .id(reserve.getId())
                    .build();
        }
    }

    @Transactional
    @Override
    public @NonNull Account commit(@NonNull Reserve reserve) {
        var account = accountService.getActiveUnsafe(reserve.getAccountId());
        return commit(reserve, account);
    }

    @Transactional
    public @NonNull Account commit(@NonNull Reserve reserve, @NonNull Account account) {
        var reserveType = reserve.getType();
        if (!reserveService.delete(reserve)) {
            throw new AlreadyHandledReserveException(reserve);
        }
        switch (reserveType) {
            case DEPOSIT:
                return accountService.incr(account.getId(), reserve.getAmount());
            case WITHDRAW:
                return accountService.decr(account.getId(), reserve.getAmount());
            default:
                var msg = String.format("unknown reserve type. %s", reserve.getType());
                throw new IllegalArgumentException(msg);
        }
    }

    @Transactional
    @Override
    public void rollback(@NonNull Reserve reserve) {
        if (!reserveService.delete(reserve)) {
            throw new AlreadyHandledReserveException(reserve);
        }
    }
}
