package com.fireforest.simulation.api;

import com.fireforest.simulation.api.dto.ParametersDto;
import com.fireforest.simulation.api.dto.SimulationRequest;
import com.fireforest.simulation.api.dto.SimulationResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API consumed by the web front end. */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /** Default parameters, as defined in the configuration file of the program. */
    @GetMapping("/configuration")
    public ParametersDto configuration() {
        return ParametersDto.from(simulationService.defaultParameters());
    }

    /** Runs a complete simulation and returns every intermediate state. */
    @PostMapping(value = "/simulations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SimulationResponse simulate(@RequestBody(required = false) SimulationRequest request) {
        return simulationService.simulate(request);
    }
}
