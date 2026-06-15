package com.geonews.integration.api.dto;

public record AuthResponse(
        String token,
        UserDto user
) {
}
