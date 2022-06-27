package dev.platonov.bank.accountapi.services;

import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ReserveIdValidator { // TODO: 16.06.2022 sync with @Valid?
    public void throwIfInvalid(@NonNull String id) {
        // TODO: 28.06.2022 impl
    }
}
