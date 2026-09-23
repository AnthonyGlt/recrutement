package com.fireforest.simulation.config;

/** Raised when the configuration file is missing, malformed or holds invalid parameters. */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
