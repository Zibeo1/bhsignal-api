package com.geonews.integration.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 120) String name,
        @Size(min = 6, max = 100) String password
) {
}
