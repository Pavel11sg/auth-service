package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final UserCredentialsRepository userCredentialsRepository;

	public CustomUserDetailsService(UserCredentialsRepository userCredentialsRepository) {
		this.userCredentialsRepository = userCredentialsRepository;
	}

	public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
		UserCredentials user = userCredentialsRepository.findByUsernameWithRoles(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new UserDetailsImpl(user);
	}
}
