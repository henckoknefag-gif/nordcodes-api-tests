package nordcodes.qa.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.Immutable;

@Immutable
@CheckReturnValue
public record ApiResponse(int status, String contentType, String body) {
  private static final ObjectMapper JSON = new ObjectMapper();

  public JsonNode json() {
    try {
      return JSON.readTree(body);
    } catch (JsonProcessingException error) {
      throw new AssertionError(
          "Приложение должно вернуть читаемый JSON, но вернуло: " + body, error);
    }
  }
}
