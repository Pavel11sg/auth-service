package com.example.tasks.authenticationservice.integration.controller;

import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogoutControllerIT extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtils jwtUtils;
	private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private final String testUsername = "testuser";
	private final String testPassword = "SecurePass123!";
	private String validAccessToken;

	@Test
	@Sql("/sql/insert_test_user.sql")
	void logout_ValidToken_ShouldRevokeToken() throws Exception {
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(testUsername)
				.password(testPassword)
				.build();
		UserDetailsImpl userDetails = new UserDetailsImpl(userCredentials);
		validAccessToken = jwtUtils.generateAccessToken(userDetails);
		assertTrue(jwtUtils.validateAccessToken(validAccessToken));
		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer " + validAccessToken))
				.andExpect(status().isNoContent());
		assertThrows(Exception.class, () -> jwtUtils.validateAccessToken(validAccessToken));
	}

	@Test
	void logout_InvalidToken_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/auth/logout")
						.header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhZTY1ODcyYS05MGZlLTQyZGItODUxZi1iOTcyZmVjYjQyZjkiLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc1NDU0OTcxMiwiZXhwIjoxNzU0NTUwMzEyfQ.kCoAw1Ye-rPd3g_zP7gd_g2223GlqW6X5B43hDOCB3k"))
				.andExpect(status().isUnauthorized());
	}
}