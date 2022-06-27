package dev.platonov.bank.accountapi.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReserveConflictException extends RuntimeException {
    public ReserveConflictException() {
    }

    public ReserveConflictException(String message) {
        super(message);
    }

    public ReserveConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReserveConflictException(Throwable cause) {
        super(cause);
    }

    public ReserveConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
