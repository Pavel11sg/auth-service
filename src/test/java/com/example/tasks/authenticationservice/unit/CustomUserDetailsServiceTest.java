package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import com.example.tasks.authenticationservice.repository.UserCredentialsRepository;
import com.example.tasks.authenticationservice.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserCredentialsRepository userCredentialsRepository;

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	private final UUID testUserId = UUID.randomUUID();
	private final LocalDateTime testTime = LocalDateTime.now();

	private Set<Role> createUserRoles() {
		Set<Role> roles = new HashSet<>();
		Role role = new Role();
		role.setRoleId(UUID.randomUUID());
		role.setName(RoleName.USER);
		roles.add(role);
		return roles;
	}

	@Test
	void loadUserByUsername_shouldReturnUserDetailsWithCorrectFields() {
		// Arrange
		String username = "testUser";
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(username)
				.password("encodedPassword")
				.email("test@example.com")
				.enabled(true)
				.accountNonLocked(true)
				.lastPasswordChange(testTime)
				.createdAt(testTime)
				.roles(createUserRoles())
				.build();

		// Mock the correct method - findByUsernameWithRoles
		when(userCredentialsRepository.findByUsernameWithRoles(username))
				.thenReturn(Optional.of(userCredentials));

		// Act
		UserDetails result = customUserDetailsService.loadUserByUsername(username);

		// Assert
		assertThat(result).isNotNull()
				.isInstanceOf(UserDetailsImpl.class);
		UserDetailsImpl userDetails = (UserDetailsImpl) result;
		assertThat(userDetails.getUsername()).isEqualTo(username);
		assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
		assertThat(userDetails.isEnabled()).isTrue();
		assertThat(userDetails.isAccountNonLocked()).isTrue();
		assertThat(userDetails.isAccountNonExpired()).isTrue();
		assertThat(userDetails.isCredentialsNonExpired()).isTrue();

		Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
		assertThat(authorities)
				.hasSize(1)
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_USER");

		assertThat(userDetails.getUserCredentials())
				.isEqualTo(userCredentials);
	}

	@Test
	void loadUserByUsername_shouldThrowException_whenUserNotFound() {
		// Arrange
		String username = "nonExistingUser";
		// Mock the correct method - findByUsernameWithRoles
		when(userCredentialsRepository.findByUsernameWithRoles(username))
				.thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("User not found");
	}

	@Test
	void loadUserByUsername_shouldReturnDisabledUser_whenUserNotEnabled() {
		// Arrange
		String username = "disabledUser";
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(username)
				.password("encodedPassword")
				.email("disabled@example.com")
				.enabled(false)
				.accountNonLocked(true)
				.roles(createUserRoles())
				.build();

		// Mock the correct method - findByUsernameWithRoles
		when(userCredentialsRepository.findByUsernameWithRoles(username))
				.thenReturn(Optional.of(userCredentials));

		// Act
		UserDetails result = customUserDetailsService.loadUserByUsername(username);

		// Assert
		assertThat(result.isEnabled()).isFalse();
	}

	@Test
	void loadUserByUsername_shouldReturnLockedAccount_whenAccountIsLocked() {
		// Arrange
		String username = "lockedUser";
		UserCredentials userCredentials = UserCredentials.builder()
				.userId(testUserId)
				.username(username)
				.password("encodedPassword")
				.email("locked@example.com")
				.enabled(true)
				.accountNonLocked(false)
				.roles(createUserRoles())
				.build();

		// Mock the correct method - findByUsernameWithRoles
		when(userCredentialsRepository.findByUsernameWithRoles(username))
				.thenReturn(Optional.of(userCredentials));

		// Act
		UserDetails result = customUserDetailsService.loadUserByUsername(username);

		// Assert
		assertThat(result.isAccountNonLocked()).isFalse();
	}
}