package com.example.tasks.authenticationservice.exception;

public class UserCredentialsNotFoundException extends RuntimeException {
	public UserCredentialsNotFoundException(String message) {
		super(message);
	}
}
