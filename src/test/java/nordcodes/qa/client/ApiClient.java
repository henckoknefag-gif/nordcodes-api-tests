package nordcodes.qa.client;

import com.google.errorprone.annotations.CheckReturnValue;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

@CheckReturnValue
public final class ApiClient {
  private final URI endpoint;
  private final HttpClient client =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(3))
          .version(HttpClient.Version.HTTP_1_1)
          .build();

  public ApiClient(URI baseUrl) {
    endpoint = baseUrl.resolve("/endpoint");
  }

  public ApiResponse send(Map<String, String> form, String apiKey) {
    var body = new StringJoiner("&");
    for (var field : form.entrySet()) {
      body.add(
          URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8)
              + "="
              + URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8));
    }
    var request =
        HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(12))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
    if (apiKey != null) {
      request.header("X-Api-Key", apiKey);
    }
    try {
      var response =
          client.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      return new ApiResponse(
          response.statusCode(),
          response.headers().firstValue("Content-Type").orElse(""),
          response.body());
    } catch (IOException error) {
      throw new AssertionError("Не удалось получить ответ приложения", error);
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      throw new AssertionError("Ожидание ответа приложения прервано", error);
    }
  }
}
