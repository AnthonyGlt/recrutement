package com.fireforest.simulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The simulation engine.
 *
 * <p>Transition rule applied to every cell burning at step {@code t}, to get step {@code t + 1}:
 * <ul>
 *   <li>the burning cell turns into ashes and can never burn again;</li>
 *   <li>each of its four adjacent cells still holding a tree catches fire with a probability
 *       {@code p}, drawn independently.</li>
 * </ul>
 *
 * <p>A cell surrounded by several burning cells therefore gets one independent chance per burning
 * neighbour. The run stops as soon as no cell is burning any more.
 */
public final class FireSimulation {

    private static final Logger LOG = LoggerFactory.getLogger(FireSimulation.class);

    private final Forest initialForest;
    private final FirePropagation propagation;
    private final int maxSteps;

    public FireSimulation(Forest initialForest, FirePropagation propagation, int maxSteps) {
        this.initialForest = Objects.requireNonNull(initialForest, "initialForest");
        this.propagation = Objects.requireNonNull(propagation, "propagation");
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("maxSteps must be strictly positive, got " + maxSteps);
        }
        this.maxSteps = maxSteps;
    }

    /** Builds a simulation from the configured parameters, using a probabilistic propagation. */
    public static FireSimulation from(SimulationParameters parameters) {
        Objects.requireNonNull(parameters, "parameters");
        return new FireSimulation(
                parameters.initialForest(),
                new ProbabilisticFirePropagation(
                        parameters.propagationProbability(), parameters.randomGenerator()),
                parameters.maxSteps());
    }

    /** Runs the simulation until the fire goes out and returns every intermediate state. */
    public SimulationResult run() {
        List<Forest> states = new ArrayList<>();
        states.add(initialForest);

        Forest current = initialForest;
        int step = 0;
        while (current.hasBurningCells() && step < maxSteps) {
            current = nextStep(current);
            states.add(current);
            step++;
            LOG.debug("step {} - {} cells burning", step, current.burningCells().size());
        }
        return new SimulationResult(states, current.hasBurningCells());
    }

    /** Computes the state at {@code t + 1} from the state at {@code t}. */
    public Forest nextStep(Forest current) {
        Objects.requireNonNull(current, "current");
        CellState[][] next = current.copyOfCells();

        for (Position burning : current.burningCells()) {
            // The fire dies out where it is burning: the cell is filled with ash.
            next[burning.row()][burning.column()] = CellState.ASH;

            for (Position neighbour : burning.orthogonalNeighbours()) {
                if (!current.contains(neighbour)) {
                    continue;
                }
                // Only a standing tree can catch fire: ashes stay ashes, and a cell already burning
                // at step t turns into ash at t + 1 whatever its neighbours do.
                if (current.stateAt(neighbour) != CellState.TREE) {
                    continue;
                }
                if (propagation.spreads(burning, neighbour)) {
                    next[neighbour.row()][neighbour.column()] = CellState.BURNING;
                }
            }
        }
        return Forest.of(next);
    }
}
