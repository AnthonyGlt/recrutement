package com.fireforest.simulation.api;

import com.fireforest.simulation.api.dto.CellDto;
import com.fireforest.simulation.api.dto.SimulationRequest;
import com.fireforest.simulation.api.dto.SimulationResponse;
import com.fireforest.simulation.config.SimulationConfigLoader;
import com.fireforest.simulation.domain.FireSimulation;
import com.fireforest.simulation.domain.Position;
import com.fireforest.simulation.domain.SimulationParameters;
import com.fireforest.simulation.domain.SimulationResult;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Runs the simulations asked by the front end.
 *
 * <p>The parameters of the configuration file are the defaults: a request only has to carry the
 * values the user actually changed.
 */
@Service
public class SimulationService {

    private final SimulationConfigLoader configLoader;
    private final String configFile;
    private final ApiProperties apiProperties;

    public SimulationService(
            SimulationConfigLoader configLoader,
            @Value("${fire.config-file:}") String configFile,
            ApiProperties apiProperties) {
        this.configLoader = configLoader;
        this.configFile = configFile;
        this.apiProperties = apiProperties;
    }

    /** Parameters read from the configuration file, used to prefill the form of the front end. */
    public SimulationParameters defaultParameters() {
        return configFile.isBlank() ? configLoader.load() : configLoader.load(Path.of(configFile));
    }

    public SimulationResponse simulate(SimulationRequest request) {
        SimulationParameters parameters = merge(request);
        SimulationResult result = FireSimulation.from(parameters).run();
        checkResponseSize(parameters, result);
        return SimulationResponse.from(parameters, result);
    }

    private SimulationParameters merge(SimulationRequest request) {
        SimulationParameters defaults = defaultParameters();
        if (request == null) {
            return defaults;
        }
        int height = valueOrDefault(request.height(), defaults.height());
        int width = valueOrDefault(request.width(), defaults.width());
        checkDimension(height, "height");
        checkDimension(width, "width");

        List<Position> burningCells = request.initialBurningCells() == null || request.initialBurningCells().isEmpty()
                ? defaults.initialBurningCells()
                : request.initialBurningCells().stream().map(CellDto::toPosition).toList();

        return new SimulationParameters(
                height,
                width,
                valueOrDefault(request.propagationProbability(), defaults.propagationProbability()),
                burningCells,
                valueOrDefault(request.maxSteps(), defaults.maxSteps()),
                // No seed in the request means an unseeded, non reproducible run.
                request.randomSeed());
    }

    private void checkDimension(int value, String name) {
        if (value > apiProperties.maxDimension()) {
            throw new IllegalArgumentException(
                    "The " + name + " must not exceed " + apiProperties.maxDimension() + ", got " + value);
        }
    }

    /** Keeps the JSON payload reasonable: a huge grid burning for a long time is refused. */
    private void checkResponseSize(SimulationParameters parameters, SimulationResult result) {
        long cells = (long) result.states().size() * parameters.height() * parameters.width();
        if (cells > apiProperties.maxReturnedCells()) {
            throw new IllegalArgumentException(
                    "This run produced " + cells + " cells to return, which exceeds the limit of "
                            + apiProperties.maxReturnedCells() + ". Use a smaller forest or fewer steps.");
        }
    }

    private static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
