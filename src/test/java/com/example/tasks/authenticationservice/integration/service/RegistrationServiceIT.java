package com.example.tasks.authenticationservice.integration.service;


import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.exception.EmailAlreadyExistsException;
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

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	void registerUser_ShouldSuccess_WhenValidRequest() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username(TEST_USERNAME)
				.email(TEST_EMAIL)
				.password(TEST_PASSWORD)
				.build();

		RegistrationResponseDto response = registrationService.registerUser(request);

		assertNotNull(response);
		assertNotNull(response.getUserId());
		assertEquals(TEST_USERNAME, response.getUsername());
		assertEquals(TEST_EMAIL, response.getEmail());
		assertNotNull(response.getCreatedAt());
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void registerUser_ShouldThrowException_WhenUsernameExists() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username("testuser") // уже существует
				.email("new@example.com")
				.password(TEST_PASSWORD)
				.build();

		assertThrows(UsernameAlreadyExistsException.class, () -> registrationService.registerUser(request));
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

		assertThrows(EmailAlreadyExistsException.class, () -> registrationService.registerUser(request));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	void registerUser_ShouldThrowException_WhenWeakPassword() {
		RegistrationRequestDto request = RegistrationRequestDto.builder()
				.username(TEST_USERNAME)
				.email(TEST_EMAIL)
				.password("weak")
				.build();

		assertThrows(IllegalArgumentException.class, () -> registrationService.registerUser(request));
	}
}
