package com.example.tasks.authenticationservice.integration.controller;

import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TokenControllerIT extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtUtils jwtUtils;

	private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
	private final String testUsername = "testuser";
	private final String testPassword = "SecurePass123!";
	private final String testInternalSecret = "internal-gateway-secret-key-12345";
	private String validAccessToken;
	private String validRefreshToken;

	@BeforeEach
	void setUp() {
		Set<Role> roles = new HashSet<>();
		Role userRole = new Role();
		userRole.setName(RoleName.USER);
		roles.add(userRole);

		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(testUsername)
				.password(testPassword)
				.roles(roles)
				.build();
		UserDetailsImpl userDetails = new UserDetailsImpl(userCredentials);
		validAccessToken = jwtUtils.generateAccessToken(userDetails);
		validRefreshToken = jwtUtils.generateRefreshToken(userDetails);
		jwtUtils.saveRefreshToken(validRefreshToken);
	}

	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void validateToken_ValidToken_ShouldReturnValidResponse() throws Exception {
		mockMvc.perform(post("/auth/validate")
						.header("Authorization", "Bearer " + validAccessToken)
						.header("X-Internal-Secret", testInternalSecret))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid", is(true)))
				.andExpect(jsonPath("$.userId", is(testUserId.toString())))
				.andExpect(jsonPath("$.message", is("Token is valid!")));
	}

	@Test
	void validateToken_InvalidToken_ShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/auth/validate")
						.header("Authorization", "Bearer invalid.token.here")
						.header("X-Internal-Secret", testInternalSecret))
				.andExpect(status().isUnauthorized());
	}


	@Test
	@Sql(scripts = "/sql/cleanup_user_credentials.sql", executionPhase = BEFORE_TEST_METHOD)
	@Sql("/sql/insert_test_user.sql")
	void refreshToken_ValidRefreshToken_ShouldReturnNewTokens() throws Exception {
		RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
		requestDto.setRefreshToken(validRefreshToken);
		mockMvc.perform(post("/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.newAccessToken", notNullValue()))
				.andExpect(jsonPath("$.newRefreshToken", notNullValue()))
				.andExpect(jsonPath("$.accessTokenExpiresIn", notNullValue()));
	}

	@Test
	void refreshToken_InvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
		RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
		requestDto.setRefreshToken("invalid.refresh.token");
		mockMvc.perform(post("/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshToken_InvalidRequest_ShouldReturnBadRequest() throws Exception {
		RefreshTokenRequestDto invalidRequest = new RefreshTokenRequestDto();
		invalidRequest.setRefreshToken("");
		mockMvc.perform(post("/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void validateToken_MissingInternalSecret_ShouldReturnForbidden() throws Exception {
		mockMvc.perform(post("/auth/validate")
						.header("Authorization", "Bearer " + validAccessToken))
				.andExpect(status().isForbidden());
	}
}