package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.DenyReason;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.repositories.ReserveRepository;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class BasicReserveService implements ReserveService {

    private final ReserveRepository repository;

    public BasicReserveService(ReserveRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveIfAllowed(@NonNull Reserve reserve) throws DeniedReserveException {
        if (BigDecimal.ZERO.compareTo(reserve.getAmount()) == 0) {
            var msg = String.format("it's not allowed to make a reserve with amount = 0. id = %s", reserve.getId());
            throw new DeniedReserveException(msg, DenyReason.EMPTY_RESERVE);
        }
        repository.saveIfAllowed(reserve);
    }

    @Override
    public @NonNull Optional<Reserve> delete(@NonNull String id) {
        return get(id)
                .map(reserve -> {
                    delete(reserve);
                    return reserve;
                });
    }

    @Override
    public boolean delete(@NonNull Reserve reserve) {
        return repository.delete(reserve);
    }

    @Override
    public @NonNull Reserve getExisting(@NonNull String id) {
        return get(id)
                .orElseThrow(() -> {
                    var msg = String.format("cannot find reserve by id = %s", id);
                    throw new IllegalArgumentException(msg);
                });
    }

    @Override
    public @NonNull Optional<Reserve> get(@NonNull String id) {
        return repository.get(id);
    }

    @Override
    public boolean exists(@NonNull String id) {
        return repository.exists(id);
    }
}
