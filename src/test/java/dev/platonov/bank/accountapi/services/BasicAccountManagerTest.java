package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.TestWithPostgresContainer;
import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.repositories.ReserveRepository;
import dev.platonov.bank.accountapi.repositories.ThreadSafetyReserveRepositoryTest;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql({"/data.sql"})
class BasicAccountManagerTest extends TestWithPostgresContainer implements ThreadSafetyAccountManagerTest { // TODO: 27.06.2022 add common tests
    private static final long ACCOUNT_ID = 1; // sync with test/resources/data.sql
    private static final BigDecimal ACCOUNT_AMOUNT = new BigDecimal(1000);
    private static final long NOT_EXISTING_ACCOUNT_ID = 0;
    @Autowired
    AccountManager accountManager;

    @Override
    public AccountManager getAccountManager() {
        return accountManager;
    }
}
