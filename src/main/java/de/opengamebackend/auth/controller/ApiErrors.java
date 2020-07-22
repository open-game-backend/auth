package de.opengamebackend.auth.controller;

import de.opengamebackend.net.ApiError;

public class ApiErrors {
    public static final ApiError ERROR_INVALID_CREDENTIALS =
            new ApiError(100, "Invalid credentials.");
    public static final ApiError ERROR_INVALID_ACCESS_TOKEN =
            new ApiError(101, "Invalid access token.");
}
