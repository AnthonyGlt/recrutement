package com.fireforest.simulation.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of the forest at a given step of the simulation.
 *
 * <p>The forest is a {@code height x width} grid of {@link CellState}. Instances are never mutated:
 * a simulation step produces a brand new {@code Forest}, which makes the whole history of a run
 * safe to keep and to expose.
 */
public final class Forest {

    private final CellState[][] cells;

    private Forest(CellState[][] cells) {
        this.cells = cells;
    }

    /**
     * Creates a forest fully covered with trees, except the given cells which are already burning.
     *
     * @param height number of rows, must be strictly positive
     * @param width  number of columns, must be strictly positive
     * @param burningCells cells on fire in the initial state, all of them inside the grid
     */
    public static Forest withInitialFires(int height, int width, Collection<Position> burningCells) {
        if (height <= 0 || width <= 0) {
            throw new IllegalArgumentException(
                    "Forest dimensions must be strictly positive, got " + height + "x" + width);
        }
        Objects.requireNonNull(burningCells, "burningCells");

        CellState[][] cells = new CellState[height][width];
        for (CellState[] row : cells) {
            java.util.Arrays.fill(row, CellState.TREE);
        }
        Forest forest = new Forest(cells);
        for (Position position : burningCells) {
            if (!forest.contains(position)) {
                throw new IllegalArgumentException(
                        "Burning cell " + position + " is outside of the " + height + "x" + width + " forest");
            }
            cells[position.row()][position.column()] = CellState.BURNING;
        }
        return forest;
    }

    /** Creates a forest from an explicit grid of states. The array is defensively copied. */
    public static Forest of(CellState[][] cells) {
        Objects.requireNonNull(cells, "cells");
        if (cells.length == 0 || cells[0].length == 0) {
            throw new IllegalArgumentException("Forest dimensions must be strictly positive");
        }
        int width = cells[0].length;
        CellState[][] copy = new CellState[cells.length][width];
        for (int row = 0; row < cells.length; row++) {
            if (cells[row].length != width) {
                throw new IllegalArgumentException("All the rows of the forest must have the same width");
            }
            for (int column = 0; column < width; column++) {
                copy[row][column] = Objects.requireNonNull(cells[row][column], "cell state");
            }
        }
        return new Forest(copy);
    }

    /**
     * Parses a forest from its textual representation, one string per row, using the symbols of
     * {@link CellState} ({@code T}, {@code B}, {@code A}). Mostly useful for tests and fixtures.
     */
    public static Forest fromRows(List<String> rows) {
        Objects.requireNonNull(rows, "rows");
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("A forest needs at least one row");
        }
        CellState[][] cells = new CellState[rows.size()][];
        for (int row = 0; row < rows.size(); row++) {
            String line = rows.get(row);
            cells[row] = new CellState[line.length()];
            for (int column = 0; column < line.length(); column++) {
                cells[row][column] = CellState.fromSymbol(line.charAt(column));
            }
        }
        return of(cells);
    }

    public int height() {
        return cells.length;
    }

    public int width() {
        return cells[0].length;
    }

    public boolean contains(Position position) {
        return position.row() >= 0
                && position.row() < height()
                && position.column() >= 0
                && position.column() < width();
    }

    public CellState stateAt(Position position) {
        if (!contains(position)) {
            throw new IndexOutOfBoundsException("Position " + position + " is outside of the forest");
        }
        return cells[position.row()][position.column()];
    }

    public CellState stateAt(int row, int column) {
        return stateAt(new Position(row, column));
    }

    /** Positions of every cell currently on fire, in reading order. */
    public List<Position> burningCells() {
        List<Position> burning = new ArrayList<>();
        for (int row = 0; row < height(); row++) {
            for (int column = 0; column < width(); column++) {
                if (cells[row][column] == CellState.BURNING) {
                    burning.add(new Position(row, column));
                }
            }
        }
        return List.copyOf(burning);
    }

    /** The simulation keeps running as long as this method returns {@code true}. */
    public boolean hasBurningCells() {
        return !burningCells().isEmpty();
    }

    public int count(CellState state) {
        Objects.requireNonNull(state, "state");
        int count = 0;
        for (CellState[] row : cells) {
            for (CellState current : row) {
                if (current == state) {
                    count++;
                }
            }
        }
        return count;
    }

    public int cellCount() {
        return height() * width();
    }

    /** Mutable deep copy of the grid, used to build the next step. */
    CellState[][] copyOfCells() {
        CellState[][] copy = new CellState[height()][width()];
        for (int row = 0; row < height(); row++) {
            System.arraycopy(cells[row], 0, copy[row], 0, width());
        }
        return copy;
    }

    /** Textual representation of the grid, one string per row. */
    public List<String> toRows() {
        List<String> rows = new ArrayList<>(height());
        for (CellState[] row : cells) {
            StringBuilder builder = new StringBuilder(row.length);
            for (CellState state : row) {
                builder.append(state.symbol());
            }
            rows.add(builder.toString());
        }
        return List.copyOf(rows);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Forest forest && java.util.Arrays.deepEquals(cells, forest.cells);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(cells);
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), toRows());
    }
}
