package com.fireforest.simulation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point of the application: starts the REST API used by the web front end. */
@SpringBootApplication
public class FireForestSimulationApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireForestSimulationApplication.class, args);
    }
}
