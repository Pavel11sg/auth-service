package com.example.tasks.authenticationservice.integration.repository;

import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCredentialsRepositoryIT extends BaseIntegrationTest {

	@Autowired
	private UserCredentialsRepository repository;

	private final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private final String TEST_USERNAME = "testuser";
	private final String TEST_EMAIL = "testuser@example.com";

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void findByUsername_ShouldReturnUser_WhenUserExists() {
		Optional<UserCredentials> result = repository.findByUsername(TEST_USERNAME);
		assertTrue(result.isPresent());
		assertEquals(TEST_USER_ID, result.get().getUserId());
		assertEquals(TEST_EMAIL, result.get().getEmail());
	}

	@Test
	void findByUsername_ShouldReturnEmpty_WhenUserNotExists() {
		Optional<UserCredentials> result = repository.findByUsername("nonexistent");
		assertTrue(result.isEmpty());
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void existsByUsername_ShouldReturnTrue_WhenUserExists() {
		assertTrue(repository.existsByUsername(TEST_USERNAME));
	}

	@Test
	void existsByUsername_ShouldReturnFalse_WhenUserNotExists() {
		assertFalse(repository.existsByUsername("nonexistent"));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
		assertTrue(repository.existsByEmail(TEST_EMAIL));
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
		assertFalse(repository.existsByEmail("nonexistent@example.com"));
	}
}
