package dev.platonov.bank.accountapi.exceptions;

import dev.platonov.bank.accountapi.models.DenyReason;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeniedReserveException extends Exception {

    private final @NonNull DenyReason denyReason;

    public DeniedReserveException(@NonNull DenyReason denyReason) {
        this.denyReason = denyReason;
    }

    public DeniedReserveException(String message, @NonNull DenyReason denyReason) {
        super(message);
        this.denyReason = denyReason;
    }

    public DeniedReserveException(String message, Throwable cause, @NonNull DenyReason denyReason) {
        super(message, cause);
        this.denyReason = denyReason;
    }

    public DeniedReserveException(Throwable cause, @NonNull DenyReason denyReason) {
        super(cause);
        this.denyReason = denyReason;
    }
}
