package com.fireforest.simulation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Raw content of the YAML configuration file, before any validation.
 *
 * <pre>
 * forest:
 *   height: 20
 *   width: 40
 * fire:
 *   propagationProbability: 0.55
 *   initialBurningCells:
 *     - { row: 10, column: 20 }
 * simulation:
 *   maxSteps: 1000
 *   randomSeed: 42
 * </pre>
 */
public record SimulationConfigDocument(
        @JsonProperty("forest") ForestSection forest,
        @JsonProperty("fire") FireSection fire,
        @JsonProperty("simulation") SimulationSection simulation) {

    public record ForestSection(
            @JsonProperty("height") Integer height,
            @JsonProperty("width") Integer width) {}

    public record FireSection(
            @JsonProperty("propagationProbability") Double propagationProbability,
            @JsonProperty("initialBurningCells") List<CellSection> initialBurningCells) {}

    public record CellSection(
            @JsonProperty("row") Integer row,
            @JsonProperty("column") Integer column) {}

    public record SimulationSection(
            @JsonProperty("maxSteps") Integer maxSteps,
            @JsonProperty("randomSeed") Long randomSeed) {}
}
