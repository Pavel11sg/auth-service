package com.example.tasks.authenticationservice.integration.controller;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class LoginControllerIT extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private final String testUsername = "testuser";
	private final String testPassword = "SecurePass123!";
	private final String testEmail = "testuser@example.com";
	private final String testInternalSecret = "internal-gateway-secret-key-12345";

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void login_InvalidPassword_ShouldReturnUnauthorized() throws Exception {
		LoginRequestDto requestDto = new LoginRequestDto();
		requestDto.setUsername(testUsername);
		requestDto.setPassword("wrongpassword");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_InvalidRequest_ShouldReturnBadRequest() throws Exception {
		LoginRequestDto invalidRequest = new LoginRequestDto();
		invalidRequest.setUsername("");
		invalidRequest.setPassword("");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void login_NonExistentUser_ShouldReturnUnauthorized() throws Exception {
		LoginRequestDto requestDto = new LoginRequestDto();
		requestDto.setUsername("nonexistent");
		requestDto.setPassword("password");

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_MissingInternalSecret_ShouldReturnForbidden() throws Exception {
		LoginRequestDto requestDto = new LoginRequestDto();
		requestDto.setUsername(testUsername);
		requestDto.setPassword(testPassword);

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isForbidden());
	}
}