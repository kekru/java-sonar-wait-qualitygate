package de.kekru.sonarwaitqualitygate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kekru.sonarwaitqualitygate.utils.ThrowingFunction;

public class HttpService {
  private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
  private CloseableHttpClient httpClient;
  private final ObjectMapper objectMapper;

  public HttpService() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public Header getAuthorizationHeader(String userPass) {    
    final String encoded = Base64.getEncoder().encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
    return new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
  }

  public <T> T execute(HttpRequestBase request, Class<T> targetClass) {
    final ThrowingFunction<HttpResponse, T> toJson = response -> {
      final HttpEntity entity = response.getEntity();
      if (entity == null) {
        EntityUtils.consumeQuietly(response.getEntity());
        return null;
      }

      final String content = EntityUtils.toString(entity);
      LOG.debug("HTTP Content:\n" + content);
      EntityUtils.consumeQuietly(response.getEntity());

      if (String.class.equals(targetClass)) {
        return (T) content;
      }
      return objectMapper.readValue(content, targetClass);
    };

    return execute(request, toJson);
  }

  public <T> T execute(HttpRequestBase request, ThrowingFunction<HttpResponse, T> resultTransformator) {

    try (CloseableHttpResponse response = getHttpClient().execute(request)) {
      final int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode < 200 || statusCode > 399) {
        String exceptionMessage = String.format("Request failed. Code: %s Response: %s, Request: %s", statusCode, response,request);
        if (response.getEntity() != null) {
          exceptionMessage += ", Content: " + EntityUtils.toString(response.getEntity());
        }
        throw new RuntimeException(exceptionMessage);
      }

      return resultTransformator.apply(response);
    } catch (Exception e) {
      throw new RuntimeException(String.format("Request failed. %s", request), e);
    }
  }

  protected CloseableHttpClient getHttpClient() {
    if (this.httpClient == null) {
      this.httpClient = HttpClients.createDefault();
    }

    return this.httpClient;
  }
}
