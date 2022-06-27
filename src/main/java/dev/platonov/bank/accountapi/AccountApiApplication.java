package dev.platonov.bank.accountapi;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.exceptions.ReserveNotFoundException;
import dev.platonov.bank.accountapi.models.DeniedReserveEvent;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.models.ReserveOption;
import dev.platonov.bank.accountapi.services.AccountManager;
import dev.platonov.bank.accountapi.services.ReserveIdValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
public class AccountApiApplication { // TODO: 15.06.2022 add javadocs

    public static void main(String[] args) {
        SpringApplication.run(AccountApiApplication.class, args);
    }

    @Bean
    public Consumer<ReserveOption> createReserve(AccountManager manager,
                                                 ReserveIdValidator idValidator, StreamBridge streamBridge) {
        return reserveOption -> {
            idValidator.throwIfInvalid(reserveOption.getId());
            try {
                var reserve = manager.reserve(reserveOption);
                streamBridge.send("prepared-reserve-out-0", reserve);
            } catch (DeniedReserveException e) {
                var deny = DeniedReserveEvent.builder()
                        .reserveOption(reserveOption)
                        .reason(e.getDenyReason())
                        .build();
                streamBridge.send("denied-reserve-out-0", deny);
            }
        };
    }

    @Bean
    public Function<String, Reserve> commitReserve(AccountManager manager, ReserveIdValidator idValidator) {
        return id -> {
            idValidator.throwIfInvalid(id);
            return manager.getReserve(id)
                    .map(reserve -> {
                        manager.commit(reserve);
                        return reserve;
                    }).orElseThrow(() -> ReserveNotFoundException.withId(id));
        };
    }

    @Bean
    public Function<String, Reserve> rollbackReserve(AccountManager manager, ReserveIdValidator idValidator) {
        return id -> {
            idValidator.throwIfInvalid(id);
            return manager.getReserve(id)
                    .map(reserve -> {
                        manager.rollback(reserve);
                        return reserve;
                    }).orElseThrow(() -> ReserveNotFoundException.withId(id));
        };
    }
}
