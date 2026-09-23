package com.fireforest.simulation;

import com.fireforest.simulation.api.ApiProperties;
import com.fireforest.simulation.config.SimulationConfigLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework agnostic simulation classes into the Spring context. */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApplicationConfiguration {

    @Bean
    public SimulationConfigLoader simulationConfigLoader() {
        return new SimulationConfigLoader();
    }
}
