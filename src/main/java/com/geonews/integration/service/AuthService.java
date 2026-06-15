package com.geonews.integration.service;

import com.geonews.integration.api.dto.AuthResponse;
import com.geonews.integration.api.dto.LoginRequest;
import com.geonews.integration.api.dto.RegisterRequest;
import com.geonews.integration.api.dto.UpdateUserRequest;
import com.geonews.integration.api.dto.UserDto;
import com.geonews.integration.domain.UserEntity;
import com.geonews.integration.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(CONFLICT, "An account with this email already exists");
        }

        UserEntity user = new UserEntity();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserEntity saved = userRepository.save(user);
        return new AuthResponse(tokenService.issue(saved.getId()), toDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        }

        return new AuthResponse(tokenService.issue(user.getId()), toDto(user));
    }

    /**
     * Resolves the authenticated user from a bearer token, or throws 401.
     */
    @Transactional(readOnly = true)
    public UserDto currentUser(String authorizationHeader) {
        return toDto(requireUser(authorizationHeader));
    }

    @Transactional
    public UserDto updateCurrentUser(String authorizationHeader, UpdateUserRequest request) {
        UserEntity user = requireUser(authorizationHeader);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setUpdatedAt(Instant.now());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteCurrentUser(String authorizationHeader) {
        userRepository.delete(requireUser(authorizationHeader));
    }

    private UserEntity requireUser(String authorizationHeader) {
        UUID userId;
        try {
            userId = tokenService.verify(authorizationHeader);
        } catch (SecurityException ex) {
            throw new ResponseStatusException(UNAUTHORIZED, ex.getMessage(), ex);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account no longer exists"));
    }

    private UserDto toDto(UserEntity user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
