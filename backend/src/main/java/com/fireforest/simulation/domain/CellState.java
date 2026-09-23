package com.fireforest.simulation.domain;

/**
 * The possible states of a cell of the forest.
 *
 * <p>A cell follows a one way life cycle: {@link #TREE} -> {@link #BURNING} -> {@link #ASH}.
 * Once a cell holds ashes it can never burn again.
 */
public enum CellState {

    /** The cell holds a tree that has not burnt yet: it can catch fire. */
    TREE('T'),

    /** The cell is currently on fire: at the next step it will turn to ashes. */
    BURNING('B'),

    /** The cell has already burnt: it can never catch fire again. */
    ASH('A');

    private final char symbol;

    CellState(char symbol) {
        this.symbol = symbol;
    }

    /** Single character used to render the state as text in the REST payloads. */
    public char symbol() {
        return symbol;
    }

    public static CellState fromSymbol(char symbol) {
        for (CellState state : values()) {
            if (state.symbol == symbol) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown cell symbol: " + symbol);
    }
}
