package de.kekru.sonarwaitqualitygate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kekru.sonarwaitqualitygate.config.Config;
import de.kekru.sonarwaitqualitygate.config.ConfigService;
import de.kekru.sonarwaitqualitygate.model.CeTaskResponse;
import de.kekru.sonarwaitqualitygate.model.SonarProjectStatusResponse;
import de.kekru.sonarwaitqualitygate.model.CeTaskResponse.CeTaskStatus;
import de.kekru.sonarwaitqualitygate.utils.FileService;

public class SonarWaitQualityGate {

  private static final Logger LOG = LoggerFactory.getLogger(SonarWaitQualityGate.class);
  private static final String CE_TASK_URL_KEY = "ceTaskUrl";
  private static final String DASHBOARD_URL_KEY = "dashboardUrl";
  private static final String QUALITYGATE_PASS_STATUS = "OK";

  public static void main(String[] args) {
    Config config = new ConfigService().getConfig();

    Map<String, String> reportTaskContent = FileService.readFromFileToMap(config.getWaitqualitygate().getReportTaskTxtLocation());
    if (!reportTaskContent.containsKey(CE_TASK_URL_KEY)) {
      throw new RuntimeException(
          "File does not contain '" + CE_TASK_URL_KEY + "': " + config.getWaitqualitygate().getReportTaskTxtLocation());
    }

    final String dashboardUrl = reportTaskContent.get(DASHBOARD_URL_KEY);
    LOG.info("Sonarqube Dashboard Url: " + dashboardUrl);

    HttpService httpService = new HttpService();

    final String ceTaskUrl = reportTaskContent.get(CE_TASK_URL_KEY);
    final Header authHeader = httpService.getAuthorizationHeader(config.getSonar().getLogin());
    
    final String analysisId = runWithRetries(config, () -> {
      HttpGet httpGet = new HttpGet(ceTaskUrl);
      httpGet.setHeader(authHeader);
      LOG.info("Calling: " + ceTaskUrl);
      CeTaskResponse ceTaskResponse = httpService.execute(httpGet, CeTaskResponse.class);

      CeTaskStatus status = ceTaskResponse.getTask().getStatus();
      if (status == CeTaskStatus.IN_PROGRESS || status == CeTaskStatus.PENDING) {
        LOG.info("Sonar Analysis is " + status);
        return Optional.empty();
      }
      return Optional.ofNullable(ceTaskResponse.getTask().getAnalysisId());
    });
    
    final boolean isQualityGateSuccess = runWithRetries(config, () -> {
      final String qualityGateUrl = config.getSonar().getHost() + "/api/qualitygates/project_status?analysisId=" + analysisId;
      
      final HttpGet httpGet = new HttpGet(qualityGateUrl);
      httpGet.setHeader(authHeader);
      LOG.info("Calling: " + qualityGateUrl);
      final SonarProjectStatusResponse projectStatus = httpService.execute(httpGet, SonarProjectStatusResponse.class);
  
      final String qualityGateState = projectStatus.getProjectStatus().getStatus();
      LOG.info("Quality Gate State is: " + qualityGateState);
      return Optional.of(QUALITYGATE_PASS_STATUS.equals(qualityGateState));
    });

    if (isQualityGateSuccess) {
      LOG.info("Quality Gate passed");
    } else {
      final String failureMessage = "Quality Gate Failed. See " + dashboardUrl;
      LOG.info(failureMessage);
      if (config.getWaitqualitygate().isFailOnFailedQualityGate()) {
        throw new RuntimeException("Quality Gate Failed. See " + dashboardUrl);
      };
    }
  }


  private static <T> T runWithRetries(Config config, Supplier<Optional<T>> action) {
    final int timeoutSeconds = config.getWaitqualitygate().getTimeoutSeconds();
    final long timeStart = System.currentTimeMillis();
    final long timeEnd = timeStart + (timeoutSeconds * 1000);

    while (System.currentTimeMillis() < timeEnd) {
      try {
        Optional<T> result = action.get();
        if (result.isPresent()) {
          return result.get();
        }
        Thread.sleep(config.getWaitqualitygate().getRetryIntervalSeconds());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Sleep interrupted", e);
      } catch(Exception e) {
        LOG.warn("Action failed. Retry...", e);
      }
    }

    throw new RuntimeException("Abort after " + timeoutSeconds + " seconds");
  }
}
