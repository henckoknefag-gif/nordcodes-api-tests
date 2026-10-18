package nordcodes.qa.assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nordcodes.qa.client.ApiResponse;

public final class ApiAssertions {
  private final String apiKey;

  public ApiAssertions(String apiKey) {
    this.apiKey = apiKey;
  }

  public void ok(ApiResponse response) {
    assertEquals(200, response.status(), "Ожидается HTTP 200");
    assertTrue(response.contentType().startsWith("application/json"), "Ожидается JSON");
    var json = response.json();
    assertEquals("OK", json.path("result").asText(), "Ожидается результат OK");
    assertEquals(1, json.size(), "В успешном ответе должно быть только поле result");
  }

  public void error(ApiResponse response) {
    assertTrue(response.status() >= 400 && response.status() < 600, "Ожидается HTTP-ошибка");
    assertTrue(response.contentType().startsWith("application/json"), "Ожидается JSON");
    var json = response.json();
    assertEquals("ERROR", json.path("result").asText(), "Ожидается результат ERROR");
    assertTrue(
        json.path("message").isTextual() && !json.path("message").asText().isBlank(),
        "Причина отказа должна быть непустым текстом");
    assertEquals(2, json.size(), "Ожидаются только поля result и message");
    assertFalse(response.body().contains(apiKey), "Ответ не должен раскрывать API-ключ");
  }
}
