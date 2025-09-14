package com.example.tasks.authenticationservice.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
	private final UserCredentials userCredentials;

	public UserDetailsImpl(UserCredentials userCredentials) {
		this.userCredentials = userCredentials;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return userCredentials.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
				.toList();
	}

	@Override
	public String getPassword() {
		return userCredentials.getPassword();
	}

	@Override
	public String getUsername() {
		return userCredentials.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return userCredentials.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return userCredentials.isEnabled();
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}
}
