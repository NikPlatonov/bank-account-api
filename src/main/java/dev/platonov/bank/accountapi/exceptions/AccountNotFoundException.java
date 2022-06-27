package dev.platonov.bank.accountapi.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountNotFoundException extends IllegalArgumentException { // TODO: 02.06.2022 use it everywhere
    public AccountNotFoundException() {
    }

    public AccountNotFoundException(long accountId) {
        this(String.format("cannot find account with id = %d", accountId));
    }

    public AccountNotFoundException(String s) {
        super(s);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountNotFoundException(Throwable cause) {
        super(cause);
    }
}
