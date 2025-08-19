package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenResponseDto;
import com.example.tasks.authenticationservice.dto.ValidateTokenResponseDto;
import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.exception.UserCredentialsNotFoundException;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenService {
	private final UserCredentialsRepository userCredentialsRepository;
	private final CustomUserDetailsService userDetailsService;
	private final JwtUtils jwtUtils;

	public TokenService(UserCredentialsRepository userCredentialsRepository, CustomUserDetailsService userDetailsService, JwtUtils jwtUtils) {
		this.userCredentialsRepository = userCredentialsRepository;
		this.userDetailsService = userDetailsService;
		this.jwtUtils = jwtUtils;
	}

	public ValidateTokenResponseDto validateToken(String authHeader) {
		String token = jwtUtils.parseJwt(authHeader);
		boolean isValid = jwtUtils.validateAccessToken(token);
		if (!isValid) {
			throw new InvalidTokenException("AccessToken expired or invalid");
		}
		UUID userId = jwtUtils.getUserIdFromToken(token);
		UserCredentials userCredentials = userCredentialsRepository.findById(userId)
				.orElseThrow(() -> new UserCredentialsNotFoundException("userId: " + userId + " not found in user credentials database!"));
		return ValidateTokenResponseDto.builder()
				.userId(userCredentials.getUserId())
				.isValid(true)
				.message("Token is valid!")
				.build();
	}

	public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
		String oldRefreshToken = refreshTokenRequestDto.getRefreshToken();
		boolean isValid = jwtUtils.validateRefreshToken(oldRefreshToken);
		if (!isValid) {
			throw new InvalidTokenException("RefreshToken expired or invalid");
		}
		String username = jwtUtils.getUsernameFromToken(oldRefreshToken);
		UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);
		String newAccessToken = jwtUtils.generateAccessToken(userDetails);
		String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);
		jwtUtils.removeRefreshToken(oldRefreshToken);
		jwtUtils.saveRefreshToken(newRefreshToken);
		return RefreshTokenResponseDto.builder()
				.newAccessToken(newAccessToken)
				.newRefreshToken(newRefreshToken)
				.accessTokenExpiresIn(jwtUtils.getAccessTokenExpirationMs() / 1000)
				.build();
	}
}
