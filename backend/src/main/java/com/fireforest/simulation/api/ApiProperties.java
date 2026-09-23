package com.fireforest.simulation.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Guard rails of the REST API, configured under {@code fire.api}.
 *
 * @param maxDimension      largest accepted height or width
 * @param maxReturnedCells  largest number of cells a response may carry (steps x grid size)
 */
@ConfigurationProperties(prefix = "fire.api")
public record ApiProperties(int maxDimension, int maxReturnedCells) {

    public ApiProperties {
        if (maxDimension <= 0) {
            maxDimension = 200;
        }
        if (maxReturnedCells <= 0) {
            maxReturnedCells = 5_000_000;
        }
    }
}
