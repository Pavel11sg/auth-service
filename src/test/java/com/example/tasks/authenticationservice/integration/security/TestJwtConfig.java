package com.example.tasks.authenticationservice.integration.security;

import com.example.tasks.authenticationservice.service.TokenBlacklistAndStorageService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
public class TestJwtConfig {

	@Bean
	@Primary
	public TokenBlacklistAndStorageService tokenBlacklistAndStorageService() {
		TokenBlacklistAndStorageService mockService = Mockito.mock(TokenBlacklistAndStorageService.class);
		Mockito.when(mockService.isAccessTokenRevoked(Mockito.anyString())).thenReturn(false);
		Mockito.when(mockService.isRefreshTokenStored(Mockito.anyString())).thenReturn(true);
		return mockService;
	}

	@Bean
	@Primary
	public UserDetailsService userDetailsService() {
		UserDetailsService mockService = Mockito.mock(UserDetailsService.class);
		UserDetails userDetails = User.withUsername("testuser")
				.password("password")
				.authorities("ROLE_USER")
				.build();
		Mockito.when(mockService.loadUserByUsername("testuser")).thenReturn(userDetails);
		return mockService;
	}
}