package de.kekru.sonarwaitqualitygate.config;

import java.util.HashMap;

import de.kekru.javautils.config.PropertyLoaderService;

public class ConfigService {
  private static final String PROPERTY_PREFIX = "";
  private final PropertyLoaderService propertyLoaderService = new PropertyLoaderService();

  public Config getConfig() {
    Config config = new Config();
    overrideWithEnvVars(config);
    overrideWithSystemProperties(config);
    return config;
  }

  private void overrideWithEnvVars(Config config) {
    propertyLoaderService.applyConfigProperties(
        config,
        System.getenv(),
        "_",
        PROPERTY_PREFIX
    );
  }

  private void overrideWithSystemProperties(Config config) {
    propertyLoaderService.applyConfigProperties(
        config,
        new <String, String>HashMap(System.getProperties()),
        ".",
        PROPERTY_PREFIX
    );
  }
}
