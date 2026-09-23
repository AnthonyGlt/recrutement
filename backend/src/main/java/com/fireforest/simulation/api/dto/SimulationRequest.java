package com.fireforest.simulation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Parameters of a simulation asked by the front end. Every field is optional: a missing value falls
 * back to the value of the configuration file, except {@code randomSeed}, where a missing value
 * means "a new random run". The front end prefills its form with the configured parameters, so the
 * seed of the configuration file is sent back explicitly when it is set.
 */
public record SimulationRequest(
        @JsonProperty("height") Integer height,
        @JsonProperty("width") Integer width,
        @JsonProperty("propagationProbability") Double propagationProbability,
        @JsonProperty("initialBurningCells") List<CellDto> initialBurningCells,
        @JsonProperty("maxSteps") Integer maxSteps,
        @JsonProperty("randomSeed") Long randomSeed) {}
