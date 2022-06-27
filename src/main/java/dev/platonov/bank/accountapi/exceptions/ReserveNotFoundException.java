package dev.platonov.bank.accountapi.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReserveNotFoundException extends IllegalArgumentException {
    public static ReserveNotFoundException withId(@NonNull String reserveId) {
        var msg = String.format("cannot find account with id = %s", reserveId);
        return new ReserveNotFoundException(msg);
    }

    public ReserveNotFoundException() {
    }

    public ReserveNotFoundException(String s) {
        super(s);
    }

    public ReserveNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReserveNotFoundException(Throwable cause) {
        super(cause);
    }
}
