package com.fireforest.simulation.api.dto;

import com.fireforest.simulation.domain.SimulationParameters;
import java.util.List;

/** The parameters actually used by a run, echoed back to the front end. */
public record ParametersDto(
        int height,
        int width,
        double propagationProbability,
        List<CellDto> initialBurningCells,
        int maxSteps,
        Long randomSeed) {

    public static ParametersDto from(SimulationParameters parameters) {
        return new ParametersDto(
                parameters.height(),
                parameters.width(),
                parameters.propagationProbability(),
                parameters.initialBurningCells().stream().map(CellDto::from).toList(),
                parameters.maxSteps(),
                parameters.randomSeed());
    }
}
