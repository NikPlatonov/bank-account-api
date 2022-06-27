package dev.platonov.bank.accountapi.services;

import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.models.Account;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public interface ThreadSafetyAccountManagerTest {
    long ACCOUNT_ID = 1; // sync with test/resources/data.sql
    BigDecimal ACCOUNT_AMOUNT = new BigDecimal("1000"); // sync with test/resources/data.sql
    BigDecimal regularDepositAmount = ACCOUNT_AMOUNT.add(new BigDecimal("0.25"));
    BigDecimal legalWithdrawAmount = regularDepositAmount
            .add(new BigDecimal("2.5")); // ACCOUNT_AMOUNT / (SAMPLES_NUMBER * THREAD_NUMBER)
    BigDecimal illegalWithdrawAmount = new BigDecimal(Long.MAX_VALUE); // sync with test/resources/data.sql

    BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");
    int SAMPLES_NUMBER = 100;
    int THREAD_NUMBER = 4;

    AccountManager getAccountManager();

    @RepeatedTest(3)
    default void testThreadSafety() throws InterruptedException, ExecutionException {
        var executors = Executors.newFixedThreadPool(THREAD_NUMBER);
        var futures = new ArrayList<Future<?>>(THREAD_NUMBER);

        var latch = new CountDownLatch(0);
        var accountManager = getAccountManager();

        for (int i = 0; i < THREAD_NUMBER; i++) {
            futures.add(executors.submit(new TestWorker(accountManager, latch)));
        }

        latch.countDown();

        for (var future : futures) {
            future.get();
        }

        var savedAmount = accountManager.getActiveAccount(ACCOUNT_ID)
                .map(Account::getAmount)
                .orElse(null);
        assertEquals(ZERO_AMOUNT, savedAmount);
    }

    class TestWorker implements Callable<Boolean> {

        AccountManager manager;
        CountDownLatch latch;

        public TestWorker(AccountManager manager, CountDownLatch latch) {
            this.manager = manager;
            this.latch = latch;
        }

        @Override
        public Boolean call() throws Exception {
            latch.await();

            for (int i = 0; i < SAMPLES_NUMBER; ++i) {
                var regularDeposit = manager.reserveDeposit(getNewId(), ACCOUNT_ID, regularDepositAmount);
                assertDoesNotThrow(() -> manager.commit(regularDeposit));

                var oneMoreDeposit = manager.reserveDeposit(getNewId(), ACCOUNT_ID, illegalWithdrawAmount);
                assertDoesNotThrow(() -> manager.rollback(oneMoreDeposit));

                var legalWithdraw = manager.reserveWithdraw(getNewId(), ACCOUNT_ID, legalWithdrawAmount);
                assertDoesNotThrow(() -> manager.commit(legalWithdraw));

                assertThrows(DeniedReserveException.class,
                        () -> manager.reserveWithdraw(getNewId(), ACCOUNT_ID, illegalWithdrawAmount)
                );
            }

            return true; // assertions cannot be called in Runnable, so we mock the return
        }
        private String getNewId() {
            return UUID.randomUUID().toString();
        }
    }
}
