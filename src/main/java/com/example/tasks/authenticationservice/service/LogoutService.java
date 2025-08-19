package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.exception.InvalidTokenException;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LogoutService {
	private final JwtUtils jwtUtils;

	public LogoutService(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	public void logout(String authHeader) {
		if (!StringUtils.hasText(authHeader)) {
			throw new InvalidTokenException("Authorization header is empty");
		}
		String token = jwtUtils.parseJwt(authHeader);
		jwtUtils.revokeAccessToken(token);
	}
}
