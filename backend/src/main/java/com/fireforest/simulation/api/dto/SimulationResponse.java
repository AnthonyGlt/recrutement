package com.fireforest.simulation.api.dto;

import com.fireforest.simulation.domain.SimulationParameters;
import com.fireforest.simulation.domain.SimulationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of a run: the parameters used, every intermediate state and a few statistics.
 *
 * @param stoppedByMaxSteps {@code true} when the run hit the safety net instead of stopping because
 *                          no cell was burning any more
 */
public record SimulationResponse(
        ParametersDto parameters,
        int stepCount,
        boolean stoppedByMaxSteps,
        int burntCells,
        double burntRatio,
        List<StepDto> steps) {

    public static SimulationResponse from(SimulationParameters parameters, SimulationResult result) {
        List<StepDto> steps = new ArrayList<>(result.states().size());
        for (int index = 0; index < result.states().size(); index++) {
            steps.add(StepDto.from(index, result.states().get(index)));
        }
        return new SimulationResponse(
                ParametersDto.from(parameters),
                result.stepCount(),
                result.stoppedByMaxSteps(),
                result.burntCells(),
                result.burntRatio(),
                steps);
    }
}
