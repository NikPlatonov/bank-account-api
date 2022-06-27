package dev.platonov.bank.accountapi.exceptions;

import dev.platonov.bank.accountapi.models.Reserve;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlreadyHandledReserveException extends RuntimeException {
    public AlreadyHandledReserveException(@NonNull Reserve reserve) {
        this(
                String.format("reserve id = %s, account id = %d", reserve.getId(), reserve.getAccountId())
        );
    }
    public AlreadyHandledReserveException() {
    }

    public AlreadyHandledReserveException(String s) {
        super(s);
    }

    public AlreadyHandledReserveException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyHandledReserveException(Throwable cause) {
        super(cause);
    }
}
