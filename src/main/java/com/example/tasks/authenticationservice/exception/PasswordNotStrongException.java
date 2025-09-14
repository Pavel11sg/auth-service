package com.example.tasks.authenticationservice.exception;

public class PasswordNotStrongException extends RuntimeException {
	public PasswordNotStrongException(String message) {
		super(message);
	}
}
