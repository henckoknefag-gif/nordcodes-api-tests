package nordcodes.qa.environment;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;

public final class ExternalServiceMock implements AutoCloseable {
  private final WireMockServer server =
      new WireMockServer(wireMockConfig().dynamicPort().bindAddress("127.0.0.1"));

  public void start() {
    server.start();
  }

  public String baseUrl() {
    return "http://127.0.0.1:" + server.port();
  }

  public void reset() {
    server.resetAll();
    stubExternalResponse("/auth", 200, "любой ответ");
    stubExternalResponse("/doAction", 200, "любой ответ");
  }

  public void verifyNoCalls() {
    verifyTotalCalls(0);
  }

  public void verifyTotalCalls(int count) {
    assertEquals(count, server.getAllServeEvents().size(), "Неожиданное число внешних обращений");
  }

  /**
   * Проверяет число внешних обращений, метод, заголовки и переданный токен.
   *
   * @param path путь внешнего сервиса
   * @param expectedToken ожидаемый токен пользователя
   * @param count ожидаемое число обращений
   */
  public void verifyCalls(String path, String expectedToken, int count) {
    server.verify(
        count,
        anyRequestedFor(urlEqualTo(path)).withRequestBody(equalTo("token=" + expectedToken)));
    server.verify(
        count,
        postRequestedFor(urlEqualTo(path))
            .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
            .withHeader("Accept", equalTo("application/json"))
            .withRequestBody(equalTo("token=" + expectedToken)));
  }

  /**
   * Настраивает ответ WireMock на запрос приложения во внешний сервис.
   *
   * @param path путь внешнего сервиса
   * @param status HTTP-код, который вернет WireMock
   * @param body текст ответа WireMock
   */
  public void stubExternalResponse(String path, int status, String body) {
    server.stubFor(
        post(urlEqualTo(path)).willReturn(aResponse().withStatus(status).withBody(body)));
  }

  @Override
  public void close() {
    server.stop();
  }
}
