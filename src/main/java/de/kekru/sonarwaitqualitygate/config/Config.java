package de.kekru.sonarwaitqualitygate.config;

import lombok.Data;

@Data
public class Config {
  private Sonar sonar;
  private CustomConfig waitqualitygate;

  @Data
  public static class Sonar {
    private SonarHost host;
    private String login;
  }

  @Data
  public static class SonarHost {
    private String url;
  }

  @Data
  public static class CustomConfig {
    private String reportTaskTxtLocation = "build/sonar/report-task.txt";
    private int timeoutSeconds = 300;
    private int retryIntervalSeconds = 10;
    private boolean failOnFailedQualityGate = true;
  }
}
