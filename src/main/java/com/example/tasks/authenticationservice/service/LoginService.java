package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.dto.LoginResponseDto;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;

	public LoginService(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
	}

	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequestDto.getUsername(),
						loginRequestDto.getPassword()
				)
		);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		UserCredentials user = userDetails.getUserCredentials();
		String accessToken = jwtUtils.generateAccessToken(userDetails);
		String refreshToken = jwtUtils.generateRefreshToken(userDetails);
		jwtUtils.saveRefreshToken(refreshToken);
		return LoginResponseDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.type("Bearer")
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.accessTokenExpiresIn(jwtUtils.getAccessTokenExpirationMs() / 1000)
				.build();
	}
}
