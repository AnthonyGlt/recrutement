package com.fireforest.simulation.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fireforest.simulation.domain.Position;
import com.fireforest.simulation.domain.SimulationParameters;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads the simulation parameters from a YAML configuration file.
 *
 * <p>When no explicit path is given, the file is looked up in that order:
 * <ol>
 *   <li>{@code simulation.yml} in the working directory, so the parameters can be tuned without
 *       rebuilding the application;</li>
 *   <li>{@code simulation.yml} on the classpath, the default configuration shipped with the jar.</li>
 * </ol>
 */
public class SimulationConfigLoader {

    public static final String DEFAULT_CONFIG_FILE_NAME = "simulation.yml";

    private final ObjectMapper mapper;

    public SimulationConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory())
                // A typo in the configuration file must fail loudly instead of being silently ignored.
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /** Loads the parameters from the default location. */
    public SimulationParameters load() {
        Path workingDirectoryFile = Path.of(DEFAULT_CONFIG_FILE_NAME);
        if (Files.isReadable(workingDirectoryFile)) {
            return load(workingDirectoryFile);
        }
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME)) {
            if (stream == null) {
                throw new ConfigurationException(
                        "No configuration file found: expected " + workingDirectoryFile.toAbsolutePath()
                                + " or " + DEFAULT_CONFIG_FILE_NAME + " on the classpath");
            }
            return read(stream, "classpath:" + DEFAULT_CONFIG_FILE_NAME);
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read the default configuration file", e);
        }
    }

    /** Loads the parameters from an explicit file. */
    public SimulationParameters load(Path file) {
        if (!Files.isReadable(file)) {
            throw new ConfigurationException("Configuration file not found: " + file.toAbsolutePath());
        }
        try (InputStream stream = Files.newInputStream(file)) {
            return read(stream, file.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read the configuration file " + file, e);
        }
    }

    private SimulationParameters read(InputStream stream, String origin) {
        SimulationConfigDocument document;
        try {
            document = mapper.readValue(stream, SimulationConfigDocument.class);
        } catch (JacksonException e) {
            throw new ConfigurationException(
                    "Invalid configuration file " + origin + ": " + e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read the configuration file " + origin, e);
        }
        if (document == null) {
            throw new ConfigurationException("Empty configuration file " + origin);
        }
        return toParameters(document, origin);
    }

    /** Validates the raw document and turns it into domain parameters. */
    public SimulationParameters toParameters(SimulationConfigDocument document, String origin) {
        var forest = require(document.forest(), "forest", origin);
        var fire = require(document.fire(), "fire", origin);

        int height = require(forest.height(), "forest.height", origin);
        int width = require(forest.width(), "forest.width", origin);
        double probability = require(fire.propagationProbability(), "fire.propagationProbability", origin);

        List<SimulationConfigDocument.CellSection> cells =
                require(fire.initialBurningCells(), "fire.initialBurningCells", origin);
        List<Position> initialBurningCells = new ArrayList<>(cells.size());
        for (int index = 0; index < cells.size(); index++) {
            var cell = require(cells.get(index), "fire.initialBurningCells[" + index + "]", origin);
            initialBurningCells.add(new Position(
                    require(cell.row(), "fire.initialBurningCells[" + index + "].row", origin),
                    require(cell.column(), "fire.initialBurningCells[" + index + "].column", origin)));
        }

        var simulation = Optional.ofNullable(document.simulation())
                .orElse(new SimulationConfigDocument.SimulationSection(null, null));
        int maxSteps = Optional.ofNullable(simulation.maxSteps())
                .orElse(SimulationParameters.DEFAULT_MAX_STEPS);

        try {
            return new SimulationParameters(
                    height, width, probability, initialBurningCells, maxSteps, simulation.randomSeed());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid configuration in " + origin + ": " + e.getMessage(), e);
        }
    }

    private static <T> T require(T value, String property, String origin) {
        if (value == null) {
            throw new ConfigurationException("Missing property '" + property + "' in " + origin);
        }
        return value;
    }
}
