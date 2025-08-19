package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenResponseDto;
import com.example.tasks.authenticationservice.dto.ValidateTokenResponseDto;
import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.exception.UserCredentialsNotFoundException;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.example.tasks.authenticationservice.security.JwtUtils;
import com.example.tasks.authenticationservice.service.CustomUserDetailsService;
import com.example.tasks.authenticationservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	private UserCredentialsRepository userCredentialsRepository;

	@Mock
	private CustomUserDetailsService userDetailsService;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private TokenService tokenService;

	private UserCredentials testUser;
	private UserDetails testUserDetails;
	private RefreshTokenRequestDto refreshRequest;

	@BeforeEach
	void setUp() {
		testUser = TestDataHelper.createSavedUserCredentials();
		testUserDetails = TestDataHelper.createUserDetails();
		refreshRequest = new RefreshTokenRequestDto();
		refreshRequest.setRefreshToken(TestDataHelper.REFRESH_TOKEN);
	}

	@Test
	void validateToken_WithValidToken_ReturnsValidResponse() {
		// Arrange
		when(jwtUtils.parseJwt(TestDataHelper.VALID_AUTH_HEADER)).thenReturn(TestDataHelper.ACCESS_TOKEN);
		when(jwtUtils.validateAccessToken(TestDataHelper.ACCESS_TOKEN)).thenReturn(true);
		when(jwtUtils.getUserIdFromToken(TestDataHelper.ACCESS_TOKEN)).thenReturn(TestDataHelper.USER_ID);
		when(userCredentialsRepository.findById(TestDataHelper.USER_ID))
				.thenReturn(Optional.of(testUser));
		// Act
		ValidateTokenResponseDto response = tokenService.validateToken(TestDataHelper.VALID_AUTH_HEADER);
		// Assert
		assertNotNull(response);
		assertTrue(response.isValid());
		assertEquals(TestDataHelper.USER_ID, response.getUserId());
		assertEquals("Token is valid!", response.getMessage());
		verify(jwtUtils).parseJwt(TestDataHelper.VALID_AUTH_HEADER);
		verify(jwtUtils).validateAccessToken(TestDataHelper.ACCESS_TOKEN);
		verify(jwtUtils).getUserIdFromToken(TestDataHelper.ACCESS_TOKEN);
		verify(userCredentialsRepository).findById(TestDataHelper.USER_ID);
	}

	@Test
	void validateToken_WithInvalidToken_ThrowsInvalidTokenException() {
		// Arrange
		String invalidToken = "invalid.token";
		String invalidAuthHeader = "Bearer " + invalidToken;
		when(jwtUtils.parseJwt(invalidAuthHeader)).thenReturn(invalidToken);
		when(jwtUtils.validateAccessToken(invalidToken)).thenThrow(new InvalidTokenException("Invalid token"));
		// Act & Assert
		assertThrows(InvalidTokenException.class, () -> tokenService.validateToken(invalidAuthHeader));
		verify(jwtUtils).parseJwt(invalidAuthHeader);
		verify(jwtUtils).validateAccessToken(invalidToken);
		verifyNoMoreInteractions(jwtUtils, userCredentialsRepository);
	}

	@Test
	void validateToken_WithMissingUser_ThrowsUserCredentialsNotFoundException() {
		// Arrange
		UUID nonExistentUserId = UUID.randomUUID(); // Создаем случайный несуществующий ID
		when(jwtUtils.parseJwt(TestDataHelper.VALID_AUTH_HEADER)).thenReturn(TestDataHelper.ACCESS_TOKEN);
		when(jwtUtils.validateAccessToken(TestDataHelper.ACCESS_TOKEN)).thenReturn(true);
		when(jwtUtils.getUserIdFromToken(TestDataHelper.ACCESS_TOKEN)).thenReturn(nonExistentUserId);
		when(userCredentialsRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
		// Act & Assert
		assertThrows(UserCredentialsNotFoundException.class,
				() -> tokenService.validateToken(TestDataHelper.VALID_AUTH_HEADER));
		verify(jwtUtils).parseJwt(TestDataHelper.VALID_AUTH_HEADER);
		verify(jwtUtils).validateAccessToken(TestDataHelper.ACCESS_TOKEN);
		verify(jwtUtils).getUserIdFromToken(TestDataHelper.ACCESS_TOKEN);
		verify(userCredentialsRepository).findById(nonExistentUserId);
	}

	@Test
	void refreshToken_WithValidRefreshToken_ReturnsNewTokens() {
		// Arrange
		String newAccessToken = "new-access-token";
		String newRefreshToken = "new-refresh-token";
		when(jwtUtils.validateRefreshToken(TestDataHelper.REFRESH_TOKEN)).thenReturn(true);
		when(jwtUtils.getUsernameFromToken(TestDataHelper.REFRESH_TOKEN)).thenReturn(TestDataHelper.USERNAME);
		when(userDetailsService.loadUserByUsername(TestDataHelper.USERNAME)).thenReturn((UserDetailsImpl) testUserDetails);
		when(jwtUtils.generateAccessToken((UserDetailsImpl) testUserDetails)).thenReturn(newAccessToken);
		when(jwtUtils.generateRefreshToken(testUserDetails)).thenReturn(newRefreshToken);
		when(jwtUtils.getAccessTokenExpirationMs()).thenReturn(TestDataHelper.TOKEN_EXPIRATION_MS);
		// Act
		RefreshTokenResponseDto response = tokenService.refreshToken(refreshRequest);
		// Assert
		assertNotNull(response);
		assertEquals(newAccessToken, response.getNewAccessToken());
		assertEquals(newRefreshToken, response.getNewRefreshToken());
		assertEquals(TestDataHelper.TOKEN_EXPIRATION_MS / 1000, response.getAccessTokenExpiresIn());
		verify(jwtUtils).validateRefreshToken(TestDataHelper.REFRESH_TOKEN);
		verify(jwtUtils).getUsernameFromToken(TestDataHelper.REFRESH_TOKEN);
		verify(userDetailsService).loadUserByUsername(TestDataHelper.USERNAME);
		verify(jwtUtils).generateAccessToken((UserDetailsImpl) testUserDetails);
		verify(jwtUtils).generateRefreshToken(testUserDetails);
		verify(jwtUtils).removeRefreshToken(TestDataHelper.REFRESH_TOKEN);
		verify(jwtUtils).saveRefreshToken(newRefreshToken);
		verify(jwtUtils).getAccessTokenExpirationMs();
	}

	@Test
	void refreshToken_WithInvalidRefreshToken_ThrowsInvalidTokenException() {
		// Arrange
		RefreshTokenRequestDto invalidRequest = new RefreshTokenRequestDto();
		invalidRequest.setRefreshToken("invalid.token");
		when(jwtUtils.validateRefreshToken("invalid.token")).thenThrow(new InvalidTokenException("Invalid token"));
		// Act & Assert
		assertThrows(InvalidTokenException.class, () -> tokenService.refreshToken(invalidRequest));
		verify(jwtUtils).validateRefreshToken("invalid.token");
		verifyNoMoreInteractions(jwtUtils, userDetailsService);
	}

	@Test
	void refreshToken_WithExpiredRefreshToken_ThrowsInvalidTokenException() {
		// Arrange
		when(jwtUtils.validateRefreshToken(TestDataHelper.REFRESH_TOKEN)).thenReturn(false);
		// Act & Assert
		assertThrows(InvalidTokenException.class, () -> tokenService.refreshToken(refreshRequest));
		verify(jwtUtils).validateRefreshToken(TestDataHelper.REFRESH_TOKEN);
		verifyNoMoreInteractions(jwtUtils, userDetailsService);
	}
}
