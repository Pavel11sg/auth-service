package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.dto.LoginResponseDto;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import com.example.tasks.authenticationservice.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private LoginService loginService;

	private TestDataHelper testData;

	@BeforeEach
	void setUp() {
		testData = new TestDataHelper();
	}

	@Test
	void login_WithValidCredentials_ReturnsCorrectLoginResponse() {
		// Arrange
		LoginRequestDto request = testData.createValidLoginRequest();
		UserDetailsImpl userDetails = testData.createUserDetails();
		Authentication authentication = testData.createAuthentication(userDetails);
		when(authenticationManager.authenticate(any()))
				.thenReturn(authentication);
		when(jwtUtils.generateAccessToken(userDetails))
				.thenReturn(TestDataHelper.ACCESS_TOKEN);
		when(jwtUtils.generateRefreshToken(userDetails))
				.thenReturn(TestDataHelper.REFRESH_TOKEN);
		when(jwtUtils.getAccessTokenExpirationMs())
				.thenReturn(TestDataHelper.TOKEN_EXPIRATION_MS);
		// Act
		LoginResponseDto response = loginService.login(request);
		// Assert
		assertThat(response)
				.usingRecursiveComparison()
				.ignoringFields("accessTokenExpiresIn")
				.isEqualTo(testData.createExpectedLoginResponse());
		assertThat(response.getAccessTokenExpiresIn())
				.isEqualTo(TestDataHelper.TOKEN_EXPIRATION_MS / 1000);
		verify(jwtUtils).saveRefreshToken(TestDataHelper.REFRESH_TOKEN);
	}

	@Test
	void login_ShouldCallAuthenticationManagerWithCorrectCredentials() {
		// Arrange
		LoginRequestDto request = testData.createValidLoginRequest();
		UserDetailsImpl userDetails = testData.createUserDetails();
		Authentication authentication = testData.createAuthentication(userDetails);
		when(authenticationManager.authenticate(any()))
				.thenReturn(authentication);
		when(jwtUtils.generateAccessToken(any()))
				.thenReturn(TestDataHelper.ACCESS_TOKEN);
		when(jwtUtils.generateRefreshToken(any()))
				.thenReturn(TestDataHelper.REFRESH_TOKEN);
		when(jwtUtils.getAccessTokenExpirationMs())
				.thenReturn(TestDataHelper.TOKEN_EXPIRATION_MS);
		// Act
		loginService.login(request);
		// Assert
		verify(authenticationManager).authenticate(
				argThat(token ->
						token.getPrincipal().equals(TestDataHelper.USERNAME) &&
								token.getCredentials().equals(TestDataHelper.PASSWORD)
				));
	}

	@Test
	void login_ShouldGenerateTokensWithCorrectUserDetails() {
		// Arrange
		LoginRequestDto request = testData.createValidLoginRequest();
		UserDetailsImpl userDetails = testData.createUserDetails();
		Authentication authentication = testData.createAuthentication(userDetails);
		when(authenticationManager.authenticate(any()))
				.thenReturn(authentication);
		when(jwtUtils.generateAccessToken(any()))
				.thenReturn(TestDataHelper.ACCESS_TOKEN);
		when(jwtUtils.generateRefreshToken(any()))
				.thenReturn(TestDataHelper.REFRESH_TOKEN);
		when(jwtUtils.getAccessTokenExpirationMs())
				.thenReturn(TestDataHelper.TOKEN_EXPIRATION_MS);
		// Act
		loginService.login(request);
		// Assert
		verify(jwtUtils).generateAccessToken(userDetails);
		verify(jwtUtils).generateRefreshToken(userDetails);
	}
}