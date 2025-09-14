package com.example.tasks.authenticationservice.unit;

import com.example.tasks.authenticationservice.dto.LoginRequestDto;
import com.example.tasks.authenticationservice.dto.LoginResponseDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenRequestDto;
import com.example.tasks.authenticationservice.dto.RefreshTokenResponseDto;
import com.example.tasks.authenticationservice.dto.RegistrationRequestDto;
import com.example.tasks.authenticationservice.dto.RegistrationResponseDto;
import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import com.example.tasks.authenticationservice.model.UserCredentials;
import com.example.tasks.authenticationservice.model.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.example.tasks.authenticationservice.model.Role;
import com.example.tasks.authenticationservice.model.RoleName;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestDataHelper {
	public static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	public static final String USERNAME = "testuser";
	public static final String PASSWORD = "ValidPass1!";
	public static final String EMAIL = "test@example.com";
	public static final String ACCESS_TOKEN = "test-access-token";
	public static final String REFRESH_TOKEN = "test-refresh-token";
	public static final long TOKEN_EXPIRATION_MS = 3600000L;
	public static final String VALID_AUTH_HEADER = "Bearer " + ACCESS_TOKEN;
	public static final String JTI = "123e4567-e89b-12d3-a456-426614174000";
	public static final String SECRET = "nQ9XKp3Lv6yRf8jWtEwP2bGc7ZmY5xH1v4Sd0AaBbCcDdEeFfGgHhIiJjJkKlLmMnNoPqQrRsStTuU";
	public static final String VALID_TOKEN;
	public static final String BLACKLIST_PREFIX = "auth:blacklist:";
	public static final String REFRESH_PREFIX = "auth:storage:";
	public static final int ACCESS_TOKEN_TTL_ADDITIONAL_SECONDS = 10;
	public static final int REFRESH_TOKEN_TTL_ADDITIONAL_SECONDS = 60;
	public static final String BLACKLIST_TOKEN_VALUE = "revoked";
	public static final Boolean STORAGE_TOKEN_VALUE = true;
	public static final String INTERNAL_SECRET = "valid-secret";
	public static final String NAME = "Test";
	public static final String SURNAME = "User";
	public static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
	public static final Set<Role> USER_ROLES = createUserRoles();
	static {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		VALID_TOKEN = Jwts.builder()
				.id(JTI)
				.subject(USERNAME)
				.claim("userId", USER_ID)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(key)
				.compact();
	}
	private static Set<Role> createUserRoles() {
		Set<Role> roles = new HashSet<>();
		Role role = new Role();
		role.setRoleId(UUID.randomUUID());
		role.setName(RoleName.USER);
		roles.add(role);
		return roles;
	}

	public static LoginRequestDto createValidLoginRequest() {
		LoginRequestDto request = new LoginRequestDto();
		request.setUsername(USERNAME);
		request.setPassword(PASSWORD);
		return request;
	}

	public static UserDetailsImpl createUserDetails() {
		UserCredentials user = UserCredentials.builder()
				.userId(USER_ID)
				.username(USERNAME)
				.email(EMAIL)
				.roles(USER_ROLES)
				.build();
		return new UserDetailsImpl(user);
	}

	public static Authentication createAuthentication(UserDetailsImpl userDetails) {
		return new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
	}

	public static LoginResponseDto createExpectedLoginResponse() {
		return LoginResponseDto.builder()
				.accessToken(ACCESS_TOKEN)
				.refreshToken(REFRESH_TOKEN)
				.type("Bearer")
				.userId(USER_ID)
				.username(USERNAME)
				.email(EMAIL)
				.roles(USER_ROLES.stream().map(role -> role.getName().name()).collect(Collectors.toSet()))
				.accessTokenExpiresIn(TOKEN_EXPIRATION_MS / 1000)
				.build();
	}

	public static RegistrationRequestDto createValidRegistrationRequest() {
		return RegistrationRequestDto.builder()
				.username(USERNAME)
				.email(EMAIL)
				.password(PASSWORD)

				.build();
	}

	public static RegistrationRequestDto createRegistrationRequestWithWeakPassword() {
		return RegistrationRequestDto.builder()
				.username(USERNAME)
				.email(EMAIL)
				.password("weak")
				.build();
	}

	public static UserCredentials createUserCredentials() {
		return UserCredentials.builder()
				.userId(USER_ID)
				.username(USERNAME)
				.email(EMAIL)
				.password(PASSWORD)
				.roles(USER_ROLES)
				.build();
	}

	public static UserCredentials createSavedUserCredentials() {
		return UserCredentials.builder()
				.userId(USER_ID)
				.username(USERNAME)
				.email(EMAIL)
				.roles(USER_ROLES)
				.password("encodedPassword")
				.createdAt(LocalDateTime.now())
				.build();
	}

	public static RegistrationResponseDto createRegistrationResponse() {
		return RegistrationResponseDto.builder()
				.userId(USER_ID)
				.username(USERNAME)
				.email(EMAIL)
				.createdAt(LocalDateTime.now())
				.build();
	}

	public static Role createUserRole() {
		Role role = new Role();
		role.setRoleId(UUID.randomUUID());
		role.setName(RoleName.USER);
		return role;
	}

	public static String createExpiredToken() {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.id(JTI)
				.subject(USERNAME)
				.issuedAt(new Date(System.currentTimeMillis() - 3600000))
				.expiration(new Date(System.currentTimeMillis() - 1800000))
				.signWith(key)
				.compact();
	}

	public static String createInvalidToken() {
		return "invalid.token.string";
	}

	public static String createMalformedAuthHeader() {
		return "MalformedHeader";
	}

	public static RefreshTokenRequestDto createRefreshTokenRequest() {
		RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto();
		requestDto.setRefreshToken(REFRESH_TOKEN);
		return requestDto;
	}

	public static RefreshTokenResponseDto createRefreshTokenResponse() {
		return RefreshTokenResponseDto.builder()
				.newAccessToken("new-access-token")
				.newRefreshToken("new-refresh-token")
				.accessTokenExpiresIn(TOKEN_EXPIRATION_MS / 1000)
				.build();
	}

	public long getNewTtl(String token, long additionalSec) {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		JwtParser parser = Jwts.parser()
				.verifyWith(key)
				.build();
		Jws<Claims> jws = parser.parseSignedClaims(token);
		Claims claims = jws.getPayload();
		long expirationMs = claims.getExpiration().getTime();
		long mlSecondsRemaining = (expirationMs - System.currentTimeMillis());
		return mlSecondsRemaining + additionalSec * 1000;
	}

	public String extractJtiFromToken(String token) {
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return claims.getId();
	}

	long calculateExpectedTtl(long additionalSec) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(VALID_TOKEN)
					.getPayload();
			long expirationMs = claims.getExpiration().getTime();
			return (expirationMs - System.currentTimeMillis()) + (additionalSec * 1000);
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate TTL", e);
		}
	}

	public static String getBlacklistKey() {
		return BLACKLIST_PREFIX + JTI;
	}

	public static String getRefreshKey() {
		return REFRESH_PREFIX + JTI;
	}
}