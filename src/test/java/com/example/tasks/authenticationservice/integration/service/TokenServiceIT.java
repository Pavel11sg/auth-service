package com.example.tasks.authenticationservice.integration.service;

import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenResponseDto;
import com.example.tasks.authenticationservice.dto.ValidateTokenResponseDto;
import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.example.tasks.authenticationservice.security.JwtUtils;
import com.example.tasks.authenticationservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
class TokenServiceIT extends BaseIntegrationTest {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserCredentialsRepository userCredentialsRepository;

	private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private final String testUsername = "testuser";
	private final String testPassword = "SecurePass123!";
	private String validAccessToken;
	private String validRefreshToken;
	private UserDetailsImpl testUserDetails;

	@BeforeEach
	void setUp() {
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(testUsername)
				.password(testPassword)
				.build();
		testUserDetails = new UserDetailsImpl(userCredentials);
		validAccessToken = jwtUtils.generateAccessToken(testUserDetails);
		validRefreshToken = jwtUtils.generateRefreshToken(testUserDetails);
		jwtUtils.saveRefreshToken(validRefreshToken);
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void validateToken_ShouldReturnValidResponse_WhenTokenValid() {
		ValidateTokenResponseDto response = tokenService.validateToken("Bearer " + validAccessToken);
		assertNotNull(response);
		assertTrue(response.isValid());
		assertEquals(testUserId, response.getUserId());
		assertEquals("Token is valid!", response.getMessage());
	}

	@Test
	void validateToken_ShouldThrowException_WhenTokenInvalid() {
		assertThrows(InvalidTokenException.class,
				() -> tokenService.validateToken("Bearer invalid.token"));
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void refreshToken_ShouldReturnNewTokens_WhenRefreshTokenValid() {
		RefreshTokenRequestDto request = new RefreshTokenRequestDto();
		request.setRefreshToken(validRefreshToken);
		RefreshTokenResponseDto response = tokenService.refreshToken(request);
		assertNotNull(response);
		assertNotNull(response.getNewAccessToken());
		assertNotNull(response.getNewRefreshToken());
		assertTrue(response.getAccessTokenExpiresIn() > 0);
		assertFalse(jwtUtils.validateRefreshToken(validRefreshToken));
		assertTrue(jwtUtils.validateRefreshToken(response.getNewRefreshToken()));
	}

	@Test
	void refreshToken_ShouldThrowException_WhenRefreshTokenInvalid() {
		RefreshTokenRequestDto request = new RefreshTokenRequestDto();
		request.setRefreshToken("invalid.refresh.token");
		assertThrows(InvalidTokenException.class,
				() -> tokenService.refreshToken(request));
	}
}