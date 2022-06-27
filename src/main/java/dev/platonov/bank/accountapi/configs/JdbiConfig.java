package dev.platonov.bank.accountapi.configs;

import dev.platonov.bank.accountapi.models.Account;
import dev.platonov.bank.accountapi.models.Reserve;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbiConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        var jdbi = Jdbi.create(dataSource).installPlugin(new PostgresPlugin());

        jdbi.registerRowMapper(ConstructorMapper.factory(Account.class));
        jdbi.registerRowMapper(ConstructorMapper.factory(Reserve.class));

        return jdbi;
    }

}
