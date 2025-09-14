package com.example.tasks.authenticationservice.integration.service;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.exception.EmailAlreadyExistsException;
import com.example.tasks.authenticationservice.exception.PasswordNotStrongException;
import com.example.tasks.authenticationservice.exception.UsernameAlreadyExistsException;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
class RegistrationServiceIT extends BaseIntegrationTest {

	@Autowired
	private RegistrationService registrationService;

	private final UUID TEST_USER_ID = UUID.randomUUID();
	private final String TEST_USERNAME = "john_doe";
	private final String TEST_EMAIL = "john.doe@example.com";
	private final String TEST_PASSWORD = "SecurePass123!";
	private final String VALID_SECRET = "internal-gateway-secret-key-12345";

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql")
	@Sql("/sql/insert_test_user.sql")
	void registerUser_ShouldSuccess_WhenValidRequest() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username("newUser")
				.email("userEmail@example.com")
				.password(TEST_PASSWORD)
				.build();

		RegistrationResponseDto response = registrationService.registerUser(request, TEST_USER_ID.toString(), VALID_SECRET);

		assertNotNull(response);
		assertNotNull(response.getUserId());
		assertEquals("newUser", response.getUsername());
		assertEquals("userEmail@example.com", response.getEmail());
		assertNotNull(response.getCreatedAt());
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void registerUser_ShouldThrowException_WhenUsernameExists() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username("testuser")
				.email("new@example.com")
				.password(TEST_PASSWORD)
				.build();

		assertThrows(UsernameAlreadyExistsException.class,
				() -> registrationService.registerUser(request, UUID.randomUUID().toString(), VALID_SECRET));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void registerUser_ShouldThrowException_WhenEmailExists() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username("newuser")
				.email("testuser@example.com")
				.password(TEST_PASSWORD)
				.build();

		assertThrows(EmailAlreadyExistsException.class,
				() -> registrationService.registerUser(request, UUID.randomUUID().toString(), VALID_SECRET));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	void registerUser_ShouldThrowException_WhenWeakPassword() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username(TEST_USERNAME)
				.email(TEST_EMAIL)
				.password("weak")
				.build();

		assertThrows(PasswordNotStrongException.class,
				() -> registrationService.registerUser(request, UUID.randomUUID().toString(), VALID_SECRET));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	void registerUser_ShouldThrowException_WhenInvalidSecret() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username(TEST_USERNAME)
				.email(TEST_EMAIL)
				.password(TEST_PASSWORD)
				.build();

		assertThrows(org.springframework.web.server.ResponseStatusException.class,
				() -> registrationService.registerUser(request, TEST_USER_ID.toString(), "invalid-secret"));
	}
}