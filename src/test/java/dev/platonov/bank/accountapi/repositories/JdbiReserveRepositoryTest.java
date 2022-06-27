package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.TestWithPostgresContainer;
import dev.platonov.bank.accountapi.exceptions.AccountNotFoundException;
import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.exceptions.NotUniqueIdException;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.models.ReserveType;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql({"/data.sql"})
class JdbiReserveRepositoryTest extends TestWithPostgresContainer implements ThreadSafetyReserveRepositoryTest {

    private static final long ACCOUNT_ID = 1; // sync with test/resources/data.sql
    private static final BigDecimal ACCOUNT_AMOUNT = new BigDecimal(1000);
    private static final long NOT_EXISTING_ACCOUNT_ID = 0;

    @Autowired
    ReserveRepository repository;

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, 1, Long.MAX_VALUE})
    void saveIfAllowed_RegularCase(long amountBasis) throws DeniedReserveException {
        var deposit = Reserve.deposit(getNewId(), ACCOUNT_ID, new BigDecimal(amountBasis));
        repository.saveIfAllowed(deposit);
        assertEquals(Optional.of(deposit), repository.get(deposit.getId()));

        var withdraw = Reserve.deposit(getNewId(), ACCOUNT_ID, new BigDecimal(amountBasis));
        repository.saveIfAllowed(withdraw);
        assertEquals(Optional.of(withdraw), repository.get(withdraw.getId()));
    }

    @Test
    void saveIfAllowed_TrySaveWithSameId_Throws() throws DeniedReserveException {
        var reserve = Reserve.builder()
                .type(ReserveType.DEPOSIT)
                .amount(BigDecimal.TEN)
                .id(getNewId())
                .accountId(ACCOUNT_ID)
                .build();

        repository.saveIfAllowed(reserve);

        assertThrows(NotUniqueIdException.class, () -> repository.saveIfAllowed(reserve));
    }

    @Test
    void saveIfAllowed_NoSuchAccountId_Throws() {
        var reserve = Reserve.deposit(getNewId(), NOT_EXISTING_ACCOUNT_ID, BigDecimal.TEN);

        assertThrows(AccountNotFoundException.class, () -> repository.saveIfAllowed(reserve));
    }

//    @Test
//    void saveIfAllowed_ZeroAmount_Throws() { // TODO: 26.06.2022 replace to service test, it's not a field of the repo
//        var deposit = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.ZERO);
//
//        assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(deposit));
//
//        var withdraw = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.ZERO);
//
//        assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(withdraw));
//    }

    @Test
    void saveIfAllowed_WithdrawEntireAmountFromAccount_Ok() throws DeniedReserveException {
        var reserve = Reserve.withdraw(getNewId(), ACCOUNT_ID, ACCOUNT_AMOUNT);
        repository.saveIfAllowed(reserve);
        assertEquals(Optional.of(reserve), repository.get(reserve.getId()));
    }

    @Test
    void saveIfAllowed_WithdrawMoreThanAmountInAccount_Throws() {
        var amount = ACCOUNT_AMOUNT.add(BigDecimal.ONE);
        var reserve = Reserve.withdraw(getNewId(), ACCOUNT_ID, amount);
        assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(reserve));
    }

    @Test
    void saveIfAllowed_WithdrawMoreThanAmountInAccount_HasDeposit_Throws() throws DeniedReserveException {

        repository.saveIfAllowed(Reserve.deposit(getNewId(), ACCOUNT_ID, ACCOUNT_AMOUNT));

        var tooBigAmount = ACCOUNT_AMOUNT.add(BigDecimal.ONE);
        var tooBigReserve = Reserve.withdraw(getNewId(), ACCOUNT_ID, tooBigAmount);

        assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(tooBigReserve));
    }

    @Test
    void saveIfAllowed_SumOfAllWithdrawsMoreThanAmountInAccount_Throws() throws DeniedReserveException {
        var halfAmount = ACCOUNT_AMOUNT.divide(new BigDecimal(2), RoundingMode.UP);
        var extraAmount = halfAmount.add(BigDecimal.ONE);

        repository.saveIfAllowed(Reserve.withdraw(getNewId(), ACCOUNT_ID, halfAmount));

        var extraReserve = Reserve.withdraw(getNewId(), ACCOUNT_ID, extraAmount);

        assertThrows(DeniedReserveException.class, () -> repository.saveIfAllowed(extraReserve));
    }

    @Test
    void delete_NoReserve_False() {
        var reserve = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.TEN);
        assertFalse(repository.delete(reserve));
    }

    @Test
    void delete_HasReserve_DeleteAndTrue() throws DeniedReserveException {
        var reserve = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.TEN);
        repository.saveIfAllowed(reserve);
        assertTrue(repository.delete(reserve));
    }

    @Test
    void get_NoReserve_EmptyOptional() {
        assertEquals(Optional.empty(), repository.get(getNewId()));
    }

    @Test
    void get_HasReserve_Reserve() throws DeniedReserveException {
        var reserve = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.TEN);
        repository.saveIfAllowed(reserve);
        assertEquals(Optional.of(reserve), repository.get(reserve.getId()));
    }

    @Test
    void exists_NoReserve_False() {
        assertFalse(repository.exists(getNewId()));
    }

    @Test
    void exists_HasReserve_True() throws DeniedReserveException {
        var reserve = Reserve.deposit(getNewId(), ACCOUNT_ID, BigDecimal.TEN);
        repository.saveIfAllowed(reserve);
        assertTrue(repository.exists(reserve.getId()));
    }

    private String getNewId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public @NonNull ReserveRepository getRepository() {
        return repository;
    }
}
