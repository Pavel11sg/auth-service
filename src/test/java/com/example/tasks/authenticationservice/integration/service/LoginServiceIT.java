package com.example.tasks.authenticationservice.integration.service;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.dto.LoginResponseDto;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
class LoginServiceIT extends BaseIntegrationTest {

	@Autowired
	private LoginService loginService;

	private final String TEST_USERNAME = "john_doe";
	private final String TEST_PASSWORD = "SecurePass123!";
	private final String WRONG_PASSWORD = "wrongpassword";

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql(scripts = "/sql/insert_test_user.sql")
	void login_ShouldReturnTokens_WhenCredentialsValid() {
		LoginRequestDto request = new LoginRequestDto();
		request.setUsername(TEST_USERNAME);
		request.setPassword(TEST_PASSWORD);
		LoginResponseDto response = loginService.login(request);
		assertNotNull(response);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getRefreshToken());
		assertEquals("Bearer", response.getType());
		assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), response.getUserId());
		assertEquals(TEST_USERNAME, response.getUsername());
		assertEquals("john.doe@example.com", response.getEmail());
		assertTrue(response.getAccessTokenExpiresIn() > 0);
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void login_ShouldThrowException_WhenPasswordInvalid() {
		LoginRequestDto request = new LoginRequestDto();
		request.setUsername(TEST_USERNAME);
		request.setPassword(WRONG_PASSWORD);
		assertThrows(BadCredentialsException.class, () -> loginService.login(request));
	}

	@Test
	void login_ShouldThrowException_WhenUserNotExists() {
		LoginRequestDto request = new LoginRequestDto();
		request.setUsername("nonexistent");
		request.setPassword("anypassword");
		assertThrows(BadCredentialsException.class, () -> loginService.login(request));
	}
}
