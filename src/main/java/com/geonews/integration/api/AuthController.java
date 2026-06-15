package com.geonews.integration.api;

import com.geonews.integration.api.dto.AuthResponse;
import com.geonews.integration.api.dto.LoginRequest;
import com.geonews.integration.api.dto.RegisterRequest;
import com.geonews.integration.api.dto.UpdateUserRequest;
import com.geonews.integration.api.dto.UserDto;
import com.geonews.integration.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(name = AUTHORIZATION, required = false) String authorization) {
        return authService.currentUser(authorization);
    }

    @PutMapping("/me")
    public UserDto updateMe(
            @RequestHeader(name = AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return authService.updateCurrentUser(authorization, request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(
            @RequestHeader(name = AUTHORIZATION, required = false) String authorization
    ) {
        authService.deleteCurrentUser(authorization);
        return ResponseEntity.noContent().build();
    }
}
