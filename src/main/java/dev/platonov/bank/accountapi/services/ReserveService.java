package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Reserve;
import lombok.NonNull;

import java.util.Optional;

public interface ReserveService {
    void saveIfAllowed(@NonNull Reserve reserve) throws DeniedReserveException;

    @NonNull Optional<Reserve> delete(@NonNull String id);

    boolean delete(@NonNull Reserve reserve);

    @NonNull Reserve getExisting(@NonNull String id); // throws IllegalArgumentException if reserve with id doesn't exist

    @NonNull Optional<Reserve> get(@NonNull String id);

    boolean exists(@NonNull String id);
}
