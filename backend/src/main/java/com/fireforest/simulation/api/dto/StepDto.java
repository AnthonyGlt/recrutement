package com.fireforest.simulation.api.dto;

import com.fireforest.simulation.domain.CellState;
import com.fireforest.simulation.domain.Forest;
import java.util.List;

/**
 * State of the forest at one step of the simulation.
 *
 * @param index      step number, 0 being the initial state
 * @param rows       the grid, one string per row, each character being a {@link CellState} symbol
 *                   ({@code T} tree, {@code B} burning, {@code A} ash)
 */
public record StepDto(int index, List<String> rows, int trees, int burning, int ash) {

    public static StepDto from(int index, Forest forest) {
        return new StepDto(
                index,
                forest.toRows(),
                forest.count(CellState.TREE),
                forest.count(CellState.BURNING),
                forest.count(CellState.ASH));
    }
}
