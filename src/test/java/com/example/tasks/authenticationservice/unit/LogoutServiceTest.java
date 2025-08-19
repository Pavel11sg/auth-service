package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.security.JwtUtils;
import com.example.tasks.authenticationservice.service.LogoutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {
	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private LogoutService logoutService;

	@Test
	void logout_WithValidAuthHeader_ShouldRevokeToken() {
		// Arrange
		String authHeader = TestDataHelper.VALID_AUTH_HEADER;
		when(jwtUtils.parseJwt(authHeader)).thenReturn(TestDataHelper.ACCESS_TOKEN);
		// Act
		logoutService.logout(authHeader);
		// Assert
		verify(jwtUtils).parseJwt(authHeader);
		verify(jwtUtils).revokeAccessToken(TestDataHelper.ACCESS_TOKEN);
	}

	@Test
	void logout_WithEmptyAuthHeader_ShouldThrowException() {
		// Arrange
		String emptyHeader = "";
		// Act & Assert
		assertThatThrownBy(() -> logoutService.logout(emptyHeader))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("Authorization header is empty");
		verifyNoInteractions(jwtUtils);
	}

	@Test
	void logout_WithNullAuthHeader_ShouldThrowException() {
		// Act & Assert
		assertThatThrownBy(() -> logoutService.logout(null))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("Authorization header is empty");
		verifyNoInteractions(jwtUtils);
	}

	@Test
	void logout_WithInvalidAuthHeaderFormat_ShouldPropagateException() {
		// Arrange
		String invalidHeader = "InvalidHeader";
		when(jwtUtils.parseJwt(invalidHeader))
				.thenThrow(new InvalidTokenException("Missing or invalid Authorization header"));
		// Act & Assert
		assertThatThrownBy(() -> logoutService.logout(invalidHeader))
				.isInstanceOf(InvalidTokenException.class)
				.hasMessage("Missing or invalid Authorization header");
		verify(jwtUtils).parseJwt(invalidHeader);
		verifyNoMoreInteractions(jwtUtils);
	}
}
