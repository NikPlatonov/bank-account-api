package dev.platonov.bank.accountapi.models;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
public class DeniedReserveEvent {
    @NonNull ReserveOption reserveOption;
    @NonNull DenyReason reason;

    @Builder
    public DeniedReserveEvent(@NonNull ReserveOption reserveOption, @NonNull DenyReason reason) {
        this.reserveOption = reserveOption;
        this.reason = reason;
    }
}
