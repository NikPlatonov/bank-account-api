package dev.platonov.bank.accountapi.exceptions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotUniqueIdException extends RuntimeException {

    public NotUniqueIdException() {

    }

    @Builder
    public NotUniqueIdException(@NonNull String id, @NonNull String model) {
        this(String.format("%s with id = %s already exists", model, id));
    }

    public NotUniqueIdException(String message) {
        super(message);
    }

    public NotUniqueIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotUniqueIdException(Throwable cause) {
        super(cause);
    }

    public NotUniqueIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
