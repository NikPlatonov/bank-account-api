package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.services.ThreadSafetyAccountManagerTest;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Sql({"/data.sql"})
public interface ThreadSafetyReserveRepositoryTest {

    long ACCOUNT_ID = 1; // sync with test/resources/data.sql
    int SAMPLES_NUMBER = 100;
    int THREAD_NUMBER = 4;

    @NonNull ReserveRepository getRepository();

    @Test
    default void testThreadSafety() {
        var repository = getRepository();
        var executors = Executors.newFixedThreadPool(THREAD_NUMBER);
        var futures = new ArrayList<Future<?>>(THREAD_NUMBER);

        var latch = new CountDownLatch(0);

        for (int i = 0; i < THREAD_NUMBER; i++) {
            futures.add(executors.submit(new TestWorker(repository, latch)));
        }

        latch.countDown();

        assertDoesNotThrow(() -> {
            for (var future : futures) {
                future.get();
            }
        });
    }

    class TestWorker implements Callable<Boolean> {
        static BigDecimal regularDepositAmount = new BigDecimal("1000.30");
        static BigDecimal legalWithdrawAmount = new BigDecimal("200"); // sync with parallel threads number
        static BigDecimal illegalWithdrawAmount = new BigDecimal("1000.15"); // sync with test/resources/data.sql
        ReserveRepository repository;
        CountDownLatch latch;

        public TestWorker(ReserveRepository repository, CountDownLatch latch) {
            this.repository = repository;
            this.latch = latch;
        }

        @Override
        public Boolean call() throws Exception {
            latch.await();

            for (int i = 0; i < SAMPLES_NUMBER; ++ i) {
                var regularDeposit = Reserve.deposit(getNewId(), ACCOUNT_ID, regularDepositAmount);
                assertDoesNotThrow(() -> repository.saveIfAllowed(regularDeposit));
                assertEquals(Optional.of(regularDeposit), repository.get(regularDeposit.getId()));
                assertTrue(repository.delete(regularDeposit));

                var legalWithdraw = Reserve.withdraw(getNewId(), ACCOUNT_ID, legalWithdrawAmount);
                assertDoesNotThrow(() -> repository.saveIfAllowed(legalWithdraw));
                assertEquals(Optional.of(legalWithdraw), repository.get(legalWithdraw.getId()));
                assertTrue(repository.delete(legalWithdraw));

                var illegalWithdraw = Reserve.withdraw(getNewId(), ACCOUNT_ID, illegalWithdrawAmount);
                assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(illegalWithdraw));
            }

            return true; // assertions cannot be called in Runnable, so we mock the return
        }

        String getNewId() {
            return UUID.randomUUID().toString();
        }
    }
}
