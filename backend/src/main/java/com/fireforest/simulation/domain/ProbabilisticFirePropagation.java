package com.fireforest.simulation.domain;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Standard propagation rule: the fire spreads to an adjacent cell with a fixed probability
 * {@code p}. Each adjacent cell is drawn independently.
 */
public final class ProbabilisticFirePropagation implements FirePropagation {

    private final double probability;
    private final RandomGenerator random;

    /**
     * @param probability probability {@code p} of propagation, between 0 and 1 (inclusive)
     * @param random      source of randomness, seed it to get reproducible runs
     */
    public ProbabilisticFirePropagation(double probability, RandomGenerator random) {
        if (probability < 0 || probability > 1 || Double.isNaN(probability)) {
            throw new IllegalArgumentException(
                    "Propagation probability must be between 0 and 1, got " + probability);
        }
        this.probability = probability;
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public boolean spreads(Position source, Position target) {
        if (probability == 0) {
            return false;
        }
        // nextDouble() returns a value in [0, 1), so a probability of 1 always spreads.
        return random.nextDouble() < probability;
    }

    public double probability() {
        return probability;
    }
}
