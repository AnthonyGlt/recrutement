package com.fireforest.simulation.api.dto;

/** Body returned when a request cannot be honoured. */
public record ErrorResponse(String error, String message) {}
