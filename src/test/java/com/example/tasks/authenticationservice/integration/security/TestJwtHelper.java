package com.example.tasks.authenticationservice.integration.security;

import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@TestComponent
public class TestJwtHelper {

	@Autowired
	private JwtUtils jwtUtils;

	public String generateValidAccessToken(String username) {
		return jwtUtils.generateAccessToken((UserDetailsImpl) createUserDetails(username));
	}

	public String generateValidRefreshToken(String username) {
		String token = jwtUtils.generateRefreshToken(createUserDetails(username));
		jwtUtils.saveRefreshToken(token);
		return token;
	}

	private UserDetails createUserDetails(String username) {
		return User.withUsername(username)
				.password("password")
				.authorities("ROLE_USER")
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(false)
				.build();
	}
}
