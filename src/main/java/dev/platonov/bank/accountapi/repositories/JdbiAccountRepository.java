package dev.platonov.bank.accountapi.repositories;

import dev.platonov.bank.accountapi.exceptions.AccountNotFoundException;
import dev.platonov.bank.accountapi.models.Account;
import lombok.NonNull;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Repository
public class JdbiAccountRepository implements AccountRepository {
    private static final String ID = "id";
    private static final String AMOUNT = "amount";
    private static final String ACTIVE = "active";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";
    private static final String FIELDS = "id, amount, active, created_at, updated_at ";
    private static final String INSERT = "insert into accounts (id, amount, active, created_at, updated_at) " +
            "values (nextval('accounts_id_seq'), :amount, :active, :created_at, :updated_at) " +
            "returning " + FIELDS;

    private static final String UPDATE = "update accounts " +
            "set amount = :amount, " +
            "  updated_at = :updated_at " +
            "where id = :id " +
            "   and active = true ";

    private static final String INCR = "update accounts " +
            "set amount = amount + :amount, " +
            "  updated_at = :updated_at " +
            "where id = :id " +
            "   and active = true " +
            "returning " + FIELDS;

    private static final String DECR = "update accounts " +
            "set amount = amount - :amount, " +
            "  updated_at = :updated_at " +
            "where id = :id " +
            "   and active = true " +
            "returning " + FIELDS;

    private static final String CLOSE = "update accounts " +
            "set active = false, " +
            "  updated_at = :updated_at " +
            "where id = :id" +
            "   and active = true ";

    private static final String GET_BY_ID = "select " + FIELDS +
            "from accounts " +
            "where id = :id ";

    private static final String GET_ACTIVE_BY_ID = "select " + FIELDS +
            "from accounts " +
            "where id = :id and active = true ";


    private static final String GET_1_IF_EXISTS_AND_ACTIVE_BY_ID = "select 1 as exists " +
            "from accounts " +
            "where id = :id and active = true ";

    private final Jdbi jdbi;

    public JdbiAccountRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public @NonNull Account save(@NonNull Account account) {
        return jdbi.withHandle(handle -> handle.createUpdate(INSERT)
                .bindBySqlType(AMOUNT, account.getAmount(), Types.NUMERIC)
                .bindBySqlType(ACTIVE, account.isActive(), Types.BOOLEAN)
                .bindBySqlType(CREATED_AT, prepareDt(account.getCreatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                .bindBySqlType(UPDATED_AT, prepareDt(account.getUpdatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                .executeAndReturnGeneratedKeys(ID)
                .mapTo(Account.class)
                .one());
    }

    @Override
    public void update(@NonNull Account account) {
        jdbi.useHandle(handle -> {
            if (notExistsOrNotActive(handle, account.getId())) {
                throw new AccountNotFoundException(account.getId());
            }

            handle.createUpdate(UPDATE)
                    .bindBySqlType(ID, account.getId(), Types.BIGINT)
                    .bindBySqlType(AMOUNT, account.getAmount(), Types.NUMERIC)
                    .bindBySqlType(UPDATED_AT, prepareDt(account.getUpdatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .execute();
        });
    }

    @Override
    public @NonNull Account incr(long id, @NonNull BigDecimal amount) {
        return applyToAmount(id, amount, INCR);
    }

    @Override
    public @NonNull Account decr(long id, @NonNull BigDecimal amount) {
        return applyToAmount(id, amount, DECR);
    }

    private @NonNull Account applyToAmount(long id, BigDecimal amount, String query) {
        return jdbi.inTransaction(TransactionIsolationLevel.READ_COMMITTED, handle -> {
            if (notExistsOrNotActive(handle, id)) {
                throw new AccountNotFoundException(id);
            }

            return handle.createUpdate(query)
                    .bindBySqlType(ID, id, Types.BIGINT)
                    .bindBySqlType(AMOUNT, amount, Types.NUMERIC)
                    .bindBySqlType(UPDATED_AT, prepareDt(LocalDateTime.now()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .executeAndReturnGeneratedKeys(ID)
                    .mapTo(Account.class)
                    .findOne()
                    .orElseThrow(() -> new AccountNotFoundException(id));
        });
    }

    @Override
    public void close(@NonNull Account account) {
        jdbi.useHandle(handle -> {
            if (notExistsOrNotActive(handle, account.getId())) {
                throw new AccountNotFoundException(account.getId());
            }

            handle.createUpdate(CLOSE)
                    .bindBySqlType(ID, account.getId(), Types.BIGINT)
                    .bindBySqlType(UPDATED_AT, prepareDt(account.getUpdatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                    .execute();
        });
    }

    @Override
    public @NonNull Optional<Account> get(long id) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_BY_ID)
                .bindBySqlType(ID, id, Types.BIGINT)
                .mapTo(Account.class)
                .findOne());
    }

    @Override
    public @NonNull Optional<Account> getActive(long id) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_ACTIVE_BY_ID)
                .bindBySqlType(ID, id, Types.BIGINT)
                .mapTo(Account.class)
                .findOne());
    }

    @Override
    public boolean existsAndActive(long id) {
        return jdbi.withHandle(handle -> handle.createQuery(GET_1_IF_EXISTS_AND_ACTIVE_BY_ID)
                        .bindBySqlType(ID, id, Types.BIGINT)
                        .mapTo(Integer.class)
                        .findOne())
                .isPresent();
    }

    private boolean notExistsOrNotActive(Handle handle, long accountId) {
        try (var query = handle
                .createQuery(GET_1_IF_EXISTS_AND_ACTIVE_BY_ID)
                .bindBySqlType(ID, accountId, Types.BIGINT)
        ) {
            return query
                    .mapTo(Integer.class)
                    .findOne()
                    .isEmpty();
        }
    }

    private OffsetDateTime prepareDt(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
