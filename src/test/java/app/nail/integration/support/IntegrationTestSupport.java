package app.nail.integration.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Bootstraps full Spring context backed by Testcontainers PostgreSQL and a shared WireMock server.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(TestIntegrationsConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestSupport {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16.4")
                    .withDatabaseName("postgres")
                    .withUsername("test")
                    .withPassword("test");

    protected static final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

    static {
        POSTGRES.start();
        wireMockServer.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private static final String CLEAN_SQL = """
            TRUNCATE TABLE app.audit_logs,
                           app.order_items,
                           app.shop_orders,
                           app.appointment_items,
                           app.appointments,
                           app.product_images,
                           app.products,
                           app.service_images,
                           app.services,
                           app.consumptions,
                           app.savings_pending,
                           app.savings_cards,
                           app.users
            RESTART IDENTITY CASCADE
            """;

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.schemas", () -> "app");
        registry.add("app.test.wiremock.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void resetState() {
        jdbcTemplate.execute(CLEAN_SQL);
        wireMockServer.resetAll();
    }

    @AfterAll
    static void shutdownInfrastructure() {
        wireMockServer.stop();
        POSTGRES.stop();
    }
}
