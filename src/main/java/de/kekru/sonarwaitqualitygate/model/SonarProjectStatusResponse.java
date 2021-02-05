package de.kekru.sonarwaitqualitygate.model;

import lombok.Data;

@Data
public class SonarProjectStatusResponse {
  private SonarProjectStatus projectStatus;

  @Data
  public static class SonarProjectStatus {

    private String status;
  }
}
