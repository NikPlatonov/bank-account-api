package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.exceptions.AccountNotFoundException;
import dev.platonov.bank.accountapi.exceptions.DeniedReserveException;
import dev.platonov.bank.accountapi.exceptions.NotUniqueIdException;
import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.models.DenyReason;
import dev.platonov.bank.accountapi.models.Reserve;
import dev.platonov.bank.accountapi.models.ReserveType;
import lombok.NonNull;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Repository
public class JdbiReserveRepository implements ReserveRepository {
    private static final String RETRY_HINT = "The transaction might succeed if retried.";
    private static final String ID = "id";
    private static final String ACCOUNT_ID = "account_id";
    private static final String TYPE = "type";
    private static final String AMOUNT = "amount";
    private static final String CREATED_AT = "created_at";

    private static final String INSERT = "insert into reserves (id, account_id, amount, type, created_at ) " +
            "values (:id, :account_id, :amount, :type, :created_at) ";

    private static final String DELETE = "delete from reserves where id = :id ";

    private static final String GET_ACTIVE_ACCOUNT_BY_ID = "select id, amount, active, created_at, updated_at " +
            "from accounts " +
            "where id = :account_id and active = true ";

    private static final String GET_BY_ID = "select id, account_id, amount, type, created_at " +
            "from reserves " +
            "where id = :id ";

    private static final String GET_1_IF_EXISTS_BY_ID = "select 1 as exists " +
            "from reserves " +
            "where id = :id ";

    private static final String GET_SUM_RESERVES_AMOUNT = "select sum(amount) as reserves_sum " +
            "from reserves " +
            "where account_id = :id and type = :type";

    private final Jdbi jdbi;

    public JdbiReserveRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void saveIfAllowed(@NonNull Reserve reserve) throws DeniedReserveException {
        boolean haveToRetry = false;
        try {
            jdbi.useTransaction(TransactionIsolationLevel.SERIALIZABLE, handle -> {

                throwIfIdNotUnique(handle, reserve.getId());

                var account = getAccount(handle, reserve);

                if (ReserveType.WITHDRAW.equals(reserve.getType())) {

                    throwIfIllegalWithdraw(handle, reserve, account);

                }

                handle.createUpdate(INSERT)
                        .bindBySqlType(ID, reserve.getId(), Types.VARCHAR)
                        .bindBySqlType(ACCOUNT_ID, reserve.getAccountId(), Types.BIGINT)
                        .bindBySqlType(AMOUNT, reserve.getAmount(), Types.NUMERIC)
                        .bindBySqlType(TYPE, reserve.getType().name(), Types.VARCHAR)
                        .bindBySqlType(CREATED_AT, prepareDt(reserve.getCreatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                        .execute();
            });
        } catch (JdbiException e) {

            if (containsRetryReason(e)) {
                haveToRetry = true;
            } else {
                throw e;
            }

        }

        if (haveToRetry) {
            saveIfAllowed(reserve);
        }
    }

    private boolean containsRetryReason(JdbiException e) {
        var cause = e.getCause();
        if (cause instanceof PSQLException) {

            var error = ((PSQLException) cause).getServerErrorMessage();

            return error != null
                    && RETRY_HINT.equals(error.getHint());
        }

        return false;
    }

    private void throwIfIdNotUnique(Handle handle, String id) {
        if (getOneIfExists(handle, id).isPresent()) {
            throw NotUniqueIdException.builder()
                    .id(id)
                    .model("reserve")
                    .build();
        }
    }

    private void throwIfIllegalWithdraw(Handle handle, Reserve reserve, Account account) throws DeniedReserveException {
        try (var reservedAmountQuery = handle.createQuery(GET_SUM_RESERVES_AMOUNT)
                .bindBySqlType(ID, reserve.getAccountId(), Types.BIGINT)
                .bindBySqlType(TYPE, ReserveType.WITHDRAW, Types.VARCHAR)) {

            var reservedAmount = reservedAmountQuery
                    .mapTo(BigDecimal.class)
                    .findOne()
                    .orElse(BigDecimal.ZERO);

            if (!hasEnoughMoney(account, reservedAmount, reserve.getAmount())) {
                throw new DeniedReserveException(
                        String.format("there are not enough funds in the account to perform the withdraw. " +
                                        "reserve's id = %s, account's id = %d",
                                reserve.getId(), reserve.getAccountId()),
                        DenyReason.NOT_ENOUGH_MONEY);
            }

        }
    }

    private boolean hasEnoughMoney(Account account, BigDecimal reserved, BigDecimal reserving) {
        var remainder = account.getAmount()
                .subtract(
                        reserved.add(reserving)
                );
        return remainder.compareTo(BigDecimal.ZERO) >= 0;
    }

    private Account getAccount(Handle handle, Reserve reserve) {
        try (var query = handle.createQuery(GET_ACTIVE_ACCOUNT_BY_ID)
                .bindBySqlType(ACCOUNT_ID, reserve.getAccountId(), Types.BIGINT)) {
            return query.mapTo(Account.class)
                    .findOne()
                    .orElseThrow(() -> {
                        throw new AccountNotFoundException(reserve.getAccountId());
                    });
        }
    }

    @Override
    public boolean delete(@NonNull Reserve reserve) {
        return jdbi.withHandle(handle -> handle.createUpdate(DELETE)
                .bindBySqlType(ID, reserve.getId(), Types.VARCHAR)
                .execute()) == 1;
    }

    @Override
    public @NonNull Optional<Reserve> get(@NonNull String id) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_BY_ID)
                .bindBySqlType(ID, id, Types.VARCHAR)
                .mapTo(Reserve.class)
                .findOne());
    }

    @Override
    public boolean exists(@NonNull String id) {
        return jdbi.withHandle(handle -> getOneIfExists(handle, id))
                .isPresent();
    }

    private Optional<Integer> getOneIfExists(Handle handle, String id) {
        try(var query = handle.createQuery(GET_1_IF_EXISTS_BY_ID)
                .bindBySqlType(ID, id, Types.VARCHAR)) {
            return query.mapTo(Integer.class)
                    .findOne();
        }
    }

    private OffsetDateTime prepareDt(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
