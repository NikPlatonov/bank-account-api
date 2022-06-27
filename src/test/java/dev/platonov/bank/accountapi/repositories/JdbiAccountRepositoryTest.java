package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.TestWithPostgresContainer;
import dev.platonov.bank.accountapi.exceptions.AccountNotFoundException;
import dev.platonov.bank.accountapi.models.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbiAccountRepositoryTest extends TestWithPostgresContainer { // TODO: 27.06.2022 add test for incr and decr
    private static final long NOT_USED_ID = -1;

    @Autowired
    AccountRepository repository;

    // save

    @Test
    void save_generatesId() {
        var saved = repository.save(new Account(new BigDecimal(100)));
        assertNotNull(saved.getId());
        var nextSaved = repository.save(new Account(new BigDecimal(100)));
        assertNotNull(nextSaved.getId());

        assertNotEquals(saved.getId(), nextSaved.getId());
    }

    @Test
    void save_IfSaved_CanGetSaved() { // have been saved - can be got
        var saved = repository.save(new Account());
        var found = repository.get(saved.getId());
        assertEquals(Optional.of(saved), found);
    }


    // get

    @Test
    void get_AccountNotFound_EmptyOptional() {
        var notExisting = repository.get(NOT_USED_ID);
        assertEquals(Optional.empty(), notExisting);
    }


    // update

    @Test
    void update_AccountNotFound_Throws() {
        var account = new Account(NOT_USED_ID);
        assertThrows(AccountNotFoundException.class, () -> repository.update(account));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, 0, 100500, Long.MAX_VALUE})
    void update_SetAnyValueForAmount(long rawAmount) {
        var account = repository.save(new Account());
        account.setAmount(new BigDecimal(rawAmount));
        repository.update(account);
        assertEquals(Optional.of(account), repository.get(account.getId()));
    }


    // close

    @Test
    void close_AccountNotFound_Throws() {
        var account = new Account(NOT_USED_ID);
        assertThrows(AccountNotFoundException.class, () -> repository.close(account));
    }


    // getActive

    @Test
    void getActive_NoAccountWithId_EmptyOptional() {
        assertEquals(Optional.empty(), repository.getActive(NOT_USED_ID));
    }

    @Test
    void getActive_HasActiveAccount_Account() {
        var account = repository.save(new Account());

        assertEquals(Optional.of(account), repository.getActive(account.getId()));
    }

    @Test
    void getActive_HasClosedAccount_EmptyOptional() {
        var account = repository.save(new Account());

        repository.close(account);

        assertEquals(Optional.empty(), repository.getActive(account.getId()));
    }


    // existsAndActive

    @Test
    void existsAndActive_NoAccountWithId_EmptyOptional() {
        assertFalse(repository.existsAndActive(NOT_USED_ID));
    }

    @Test
    void existsAndActive_HasActiveAccount_Account() {
        var account = repository.save(new Account());

        assertTrue(repository.existsAndActive(account.getId()));
    }

    @Test
    void existsAndActive_HasClosedAccount_EmptyOptional() {
        var account = repository.save(new Account());

        repository.close(account);

        assertFalse(repository.existsAndActive(account.getId()));
    }
}
