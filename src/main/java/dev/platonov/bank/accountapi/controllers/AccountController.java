package dev.platonov.bank.accountapi.controllers;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.services.AccountManager;
import dev.platonov.bank.accountapi.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("accounts")
public class AccountController {

    private final AccountService service;
    private final AccountManager manager;

    public AccountController(AccountService service, AccountManager manager) {
        this.service = service;
        this.manager = manager;
    }

    @GetMapping("{id}")
    public ResponseEntity<Account> get(@PathVariable("id") long id) {
        return service.getActive(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Account> create() {
        return ResponseEntity.ok(service.create());
    }

    @PutMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable("id") long id, @RequestParam("amount") BigDecimal amount) {
        try {
            var reserve = manager.reserveDeposit(UUID.randomUUID().toString(), id, amount);

            var account = manager.commit(reserve);

            return ResponseEntity.ok(account);

        } catch (DeniedReserveException e) {

            var reason = e.getDenyReason();
            var msg = String.format("denied due to %d error [%s]", reason.code, reason.name());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(msg);
        }
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable("id") long id, @RequestParam("amount") BigDecimal amount) {
        try {
            var reserve = manager.reserveWithdraw(UUID.randomUUID().toString(), id, amount);

            var account = manager.commit(reserve);

            return ResponseEntity.ok(account);

        } catch (DeniedReserveException e) {

            var reason = e.getDenyReason();
            var msg = String.format("denied due to %d error [%s]", reason.code, reason.name());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(msg);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> close(@PathVariable("id") long id) {
        return service.getActive(id)
                .filter(account -> {
                    service.close(account);
                    return !account.isActive();
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
