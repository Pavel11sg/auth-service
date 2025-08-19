package com.example.tasks.authenticationservice.integration.security;

import com.example.tasks.authenticationservice.integration.BaseIntegrationTest;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {TestJwtConfig.class})
@ActiveProfiles("test")
class JwtFilterIT extends BaseIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtils jwtUtils;

	@Test
	@Sql("/sql/insert_test_user.sql")
	void doFilterInternal_ShouldAllowAccess_WhenValidToken() throws Exception {
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"))
				.username("testuser")
				.password("password")
				.build();
		UserDetailsImpl userDetails = new UserDetailsImpl(userCredentials);
		String token = jwtUtils.generateAccessToken(userDetails);
		mockMvc.perform(post("/auth/validate")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void doFilterInternal_ShouldDenyAccess_WhenInvalidToken() throws Exception {
		mockMvc.perform(get("/auth/validate")
						.header("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJhZTY1ODcyYS05MGZlLTQyZGItODUxZi1iOTcyZmVjYjQyZjkiLCJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTc1NDU0OTcxMiwiZXhwIjoxNzU0NTUwMzEyfQ.kCoAw1Ye-rPd3g_zP7gd_g2223GlqW6X5B43hDOCB3k"))
				.andExpect(status().isForbidden());
	}

	@Test
	void doFilterInternal_ShouldDenyAccess_WhenNoToken() throws Exception {
		mockMvc.perform(get("/auth/validate"))
				.andExpect(status().isForbidden());
	}
}