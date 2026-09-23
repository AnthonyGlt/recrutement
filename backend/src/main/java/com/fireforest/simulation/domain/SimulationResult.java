package com.fireforest.simulation.domain;

import java.util.List;
import java.util.Objects;

/**
 * Full history of a simulation: every intermediate state, from the initial one to the state where
 * no cell is burning any more.
 *
 * @param states           states of the forest, index 0 being the initial state
 * @param stoppedByMaxSteps {@code true} when the run was interrupted by the {@code maxSteps} safety
 *                          net instead of stopping naturally
 */
public record SimulationResult(List<Forest> states, boolean stoppedByMaxSteps) {

    public SimulationResult {
        Objects.requireNonNull(states, "states");
        if (states.isEmpty()) {
            throw new IllegalArgumentException("A simulation result holds at least the initial state");
        }
        states = List.copyOf(states);
    }

    /** Number of steps performed: a fire that goes out immediately counts as one step. */
    public int stepCount() {
        return states.size() - 1;
    }

    public Forest initialState() {
        return states.get(0);
    }

    public Forest finalState() {
        return states.get(states.size() - 1);
    }

    /** Number of cells burnt during the run, including the cells on fire at step 0. */
    public int burntCells() {
        return finalState().count(CellState.ASH) + finalState().count(CellState.BURNING);
    }

    /** Share of the forest turned into ashes, between 0 and 1. */
    public double burntRatio() {
        return (double) burntCells() / finalState().cellCount();
    }
}
