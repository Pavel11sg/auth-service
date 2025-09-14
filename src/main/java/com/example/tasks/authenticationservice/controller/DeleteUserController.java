package com.example.tasks.authenticationservice.controller;

import com.example.tasks.authenticationservice.service.DeleteUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/delete")
public class DeleteUserController {
	private final DeleteUserService deleteUserService;

	public DeleteUserController(DeleteUserService deleteUserService) {
		this.deleteUserService = deleteUserService;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") UUID id,
										   @RequestHeader("X-User-ID") String currentUserId,
										   @RequestHeader("X-User-Roles") String currentUserRoles) {
		UUID currentUserUUID = UUID.fromString(currentUserId);
		List<String> roles = Arrays.asList(currentUserRoles.split(","));
		if (!id.equals(currentUserUUID) && !roles.contains("ROLE_ADMIN")) {
			throw new AccessDeniedException("Not authorized to delete this user");
		}
		deleteUserService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
