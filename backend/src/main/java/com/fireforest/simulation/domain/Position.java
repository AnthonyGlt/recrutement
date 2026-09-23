package com.fireforest.simulation.domain;

import java.util.List;

/**
 * Coordinates of a cell inside the forest grid.
 *
 * <p>Rows and columns are zero based: {@code (0, 0)} is the top left cell. A position is a pure
 * value and may point outside of a grid; {@link Forest#contains(Position)} decides whether it is
 * relevant for a given forest.
 */
public record Position(int row, int column) {

    /**
     * The four adjacent positions (up, down, left, right). Diagonals are not neighbours.
     * Some of the returned positions may lie outside of the grid.
     */
    public List<Position> orthogonalNeighbours() {
        return List.of(
                new Position(row - 1, column),
                new Position(row + 1, column),
                new Position(row, column - 1),
                new Position(row, column + 1));
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}
