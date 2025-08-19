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

	@Test
	void registerUser_ValidRequest_ShouldCreateUser() throws Exception {
		RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
				.username("newuser")
				.email("newuser@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId", notNullValue()))
				.andExpect(jsonPath("$.username", is("newuser")))
				.andExpect(jsonPath("$.email", is("newuser@example.com")));
		UserCredentials savedUser = userCredentialsRepository.findByUsername("newuser").orElseThrow();
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
						.content(objectMapper.writeValueAsString(requestDto1)))
				.andExpect(status().isCreated());
		RegistrationRequestDto requestDto2 = RegistrationRequestDto.builder()
				.username("duplicateuser")
				.email("user2@example.com")
				.password("ValidPass1!")
				.build();
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
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
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}
}