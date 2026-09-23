package com.fireforest.simulation.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Validated input of a simulation: they usually come from the configuration file, but the REST API
 * can also build them from a user request.
 *
 * @param height                 number of rows of the grid
 * @param width                  number of columns of the grid
 * @param propagationProbability probability {@code p} for the fire to spread to an adjacent cell
 * @param initialBurningCells    cells already on fire at step 0, duplicates are ignored
 * @param maxSteps               safety net, the run stops once this number of steps is reached
 * @param randomSeed             optional seed, set it to replay the exact same run
 */
public record SimulationParameters(
        int height,
        int width,
        double propagationProbability,
        List<Position> initialBurningCells,
        int maxSteps,
        Long randomSeed) {

    /** Default safety net: a run can never spread further than the size of the grid anyway. */
    public static final int DEFAULT_MAX_STEPS = 10_000;

    public SimulationParameters {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException(
                    "Forest dimensions must be strictly positive, got " + height + "x" + width);
        }
        if (propagationProbability < 0 || propagationProbability > 1 || Double.isNaN(propagationProbability)) {
            throw new IllegalArgumentException(
                    "Propagation probability must be between 0 and 1, got " + propagationProbability);
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be strictly positive, got " + maxSteps);
        }
        Objects.requireNonNull(initialBurningCells, "initialBurningCells");
        if (initialBurningCells.isEmpty()) {
            throw new IllegalArgumentException("At least one cell must be on fire in the initial state");
        }
        for (Position position : initialBurningCells) {
            Objects.requireNonNull(position, "initial burning cell");
            if (position.row() < 0 || position.row() >= height
                    || position.column() < 0 || position.column() >= width) {
                throw new IllegalArgumentException(
                        "Initial burning cell " + position + " is outside of the " + height + "x" + width + " forest");
            }
        }
        initialBurningCells = List.copyOf(new LinkedHashSet<>(initialBurningCells));
    }

    public SimulationParameters(
            int height, int width, double propagationProbability, List<Position> initialBurningCells) {
        this(height, width, propagationProbability, initialBurningCells, DEFAULT_MAX_STEPS, null);
    }

    public Forest initialForest() {
        return Forest.withInitialFires(height, width, initialBurningCells);
    }

    /** A seeded generator when a seed is configured, a random one otherwise. */
    public RandomGenerator randomGenerator() {
        return randomSeed == null ? new Random() : new Random(randomSeed);
    }
}
