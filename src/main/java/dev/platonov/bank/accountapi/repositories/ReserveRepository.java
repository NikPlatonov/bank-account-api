package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Reserve;
import lombok.NonNull;

import java.util.Optional;

public interface ReserveRepository {
    void saveIfAllowed(@NonNull Reserve reserve) throws DeniedReserveException;
    boolean delete(@NonNull Reserve reserve);
    @NonNull Optional<Reserve> get(@NonNull String id);
    boolean exists(@NonNull String id);
}
