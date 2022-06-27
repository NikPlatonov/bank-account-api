package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.models.ReserveOption;
import dev.platonov.bank.accountapi.models.Reserve;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountManager {
    @NonNull Optional<Account> getActiveAccount(long id);
    @NonNull Optional<Reserve> getReserve(@NonNull String id);
    @NonNull Reserve reserve(@NonNull ReserveOption reserveOption) throws DeniedReserveException;
    @NonNull Reserve reserveDeposit(@NonNull String id, long accountId,
                                    @NonNull BigDecimal amount) throws DeniedReserveException;

    @NonNull Reserve reserveWithdraw(@NonNull String id, long accountId,
                                     @NonNull BigDecimal amount) throws DeniedReserveException;

    @NonNull Account commit(@NonNull Reserve reserve); // throws IllegalArgument if account with reserve.getAccountId() doesn't exist

    void rollback(@NonNull Reserve reserve);
}
