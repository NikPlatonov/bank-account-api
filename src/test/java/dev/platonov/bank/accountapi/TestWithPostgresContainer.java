package dev.platonov.bank.accountapi;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;


@ContextConfiguration(initializers = {TestWithPostgresContainer.Initializer.class})
public abstract class TestWithPostgresContainer {
    protected static JdbcDatabaseContainer postgresqlContainer;

    static {
        postgresqlContainer = new PostgreSQLContainer("postgres:14.3-alpine3.16")
                .withDatabaseName("accounts-api")
                .withUsername("my-name")
                .withPassword("my-pass")
                .withInitScript("schema.sql");
        postgresqlContainer.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresqlContainer.getUsername(),
                    "spring.datasource.password=" + postgresqlContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
