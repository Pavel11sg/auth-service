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
	void logout_MissingAuthorizationHeader_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(post("/auth/logout"))
				.andExpect(status().isForbidden());
	}
}