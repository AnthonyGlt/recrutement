package com.fireforest.simulation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fireforest.simulation.domain.Position;

/** Coordinates of a cell, as exchanged with the front end. */
public record CellDto(@JsonProperty("row") int row, @JsonProperty("column") int column) {

    public static CellDto from(Position position) {
        return new CellDto(position.row(), position.column());
    }

    public Position toPosition() {
        return new Position(row, column);
    }
}
