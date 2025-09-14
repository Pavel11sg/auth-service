package com.example.tasks.authenticationservice.service;

import com.example.tasks.authenticationservice.exception.UserCredentialsNotFoundException;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserService {
	private final UserCredentialsRepository userCredentialsRepository;

	public DeleteUserService(UserCredentialsRepository userCredentialsRepository) {
		this.userCredentialsRepository = userCredentialsRepository;
	}

	@Transactional
	public void deleteUser(UUID userId) {
		if (!userCredentialsRepository.existsById(userId)) {
			throw new UserCredentialsNotFoundException(
					String.format("User with id = %s not found for removal!", userId));
		}
		userCredentialsRepository.deleteById(userId);
	}
}
