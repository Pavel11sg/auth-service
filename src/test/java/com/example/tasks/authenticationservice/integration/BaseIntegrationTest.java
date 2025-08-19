package com.example.tasks.authenticationservice.integration;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@Container
	static GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
			.withExposedPorts(6379);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@BeforeAll
	static void setupDatabase() throws SQLException {
		try (Connection conn = DriverManager.getConnection(
				postgres.getJdbcUrl(),
				postgres.getUsername(),
				postgres.getPassword());
			 Statement stmt = conn.createStatement()) {

			// Очищаем все таблицы (адаптируйте под вашу схему)
			stmt.executeUpdate("DROP SCHEMA public CASCADE");
			stmt.executeUpdate("CREATE SCHEMA public");
			stmt.executeUpdate("GRANT ALL ON SCHEMA public TO " + postgres.getUsername());
		}
	}
}