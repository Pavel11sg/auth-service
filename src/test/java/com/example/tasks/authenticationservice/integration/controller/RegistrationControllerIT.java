package com.example.tasks.authenticationservice.integration.controller;

import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationControllerIT extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserCredentialsRepository userCredentialsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final String testInternalSecret = "internal-gateway-secret-key-12345";
	private final UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

	@Test
	void registerUser_ValidRequest_ShouldCreateUser() throws Exception {
		RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
				.username("newuser")
				.email("newuser@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-User-ID", testUserId.toString())
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId", notNullValue()))
				.andExpect(jsonPath("$.username", is("newuser")))
				.andExpect(jsonPath("$.email", is("newuser@example.com")));

		UserCredentials savedUser = userCredentialsRepository.findByUsernameWithRoles("newuser").orElseThrow();
		assertEquals("newuser", savedUser.getUsername());
		assertEquals("newuser@example.com", savedUser.getEmail());
		assertTrue(passwordEncoder.matches("ValidPass1!", savedUser.getPassword()));
	}

	@Test
	void registerUser_DuplicateUsername_ShouldReturnConflict() throws Exception {
		RegistrationRequestDto requestDto1 = RegistrationRequestDto.builder()
				.username("duplicateuser")
				.email("user1@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-User-ID", testUserId.toString())
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto1)))
				.andExpect(status().isCreated());

		RegistrationRequestDto requestDto2 = RegistrationRequestDto.builder()
				.username("duplicateuser")
				.email("user2@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-User-ID", UUID.randomUUID().toString())
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(requestDto2)))
				.andExpect(status().isConflict());
	}

	@Test
	void registerUser_InvalidRequest_ShouldReturnBadRequest() throws Exception {
		RegistrationRequestDto invalidRequest = RegistrationRequestDto.builder()
				.username("")
				.email("invalid-email")
				.password("short")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-User-ID", testUserId.toString())
						.header("X-Internal-Secret", testInternalSecret)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void registerUser_InvalidInternalSecret_ShouldReturnForbidden() throws Exception {
		RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
				.username("testuser")
				.email("test@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-User-ID", testUserId.toString())
						.header("X-Internal-Secret", "wrong-secret")
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isForbidden());
	}
}