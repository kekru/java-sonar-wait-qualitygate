package de.kekru.sonarwaitqualitygate.model;

import lombok.Data;

@Data
public class CeTaskResponse {

  /**
   * See https://github.com/SonarSource/sonarqube/blob/8af47608143b0282b4cf7fa4cc77d4e6ce7d3dde/sonar-ws/src/main/protobuf/ws-ce.proto#L140-L146
   */
  public enum CeTaskStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    CANCELED
  }

  private CeTask task;

  @Data
  public static class CeTask {

    private String id;
    private String type;
    private String componentId;
    private String componentKey;
    private String componentName;
    private String componentQualifier;
    private String analysisId;
    private CeTaskStatus status;
    private String submittedAt;
    private String submitterLogin;
    private String startedAt;
    private String executedAt;
    private int executionTimeMs;
    private boolean logs;
    private boolean hasScannerContext;
    private String organization;
    private int warningCount;
  }
}
