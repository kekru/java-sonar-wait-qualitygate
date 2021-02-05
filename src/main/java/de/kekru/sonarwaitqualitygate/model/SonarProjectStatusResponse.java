package de.kekru.sonarwaitqualitygate.model;

import java.util.List;

import lombok.Data;

@Data
public class SonarProjectStatusResponse {
  private SonarProjectStatus projectStatus;

  @Data
  public static class SonarProjectStatus {

    private String status;
    private List<SonarCondition> conditions;
    private List<SonarPeriod> periods;
    private String ignoredConditions;
  }

  @Data
  public static class SonarPeriod {

    private String index;
    private String mode;
    private String date;
  }

  @Data
  public static class SonarCondition {

    private String status;
    private String metricKey;
    private String comparator;
    private int periodIndex;
    private int errorThreshold;
    private int actualValue;
  }

}
