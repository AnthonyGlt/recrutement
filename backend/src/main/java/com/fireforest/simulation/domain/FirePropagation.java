package com.fireforest.simulation.domain;

/**
 * Decides whether the fire of a burning cell spreads to one of its adjacent cells.
 *
 * <p>Extracting that decision behind an interface keeps the simulation engine deterministic and
 * testable: production code uses {@link ProbabilisticFirePropagation}, tests can inject a rule that
 * always (or never) spreads.
 */
@FunctionalInterface
public interface FirePropagation {

    /**
     * @param source a cell that is burning at the current step
     * @param target an adjacent cell holding a tree
     * @return {@code true} if the target catches fire at the next step
     */
    boolean spreads(Position source, Position target);
}
