package nordcodes.qa.api.tests.negative;

import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[API] Обработка ошибки внешнего сервиса")
class ExternalServiceErrorTest extends BaseApiTest {

  @BeforeEach
  void loginUser() {
    external.stubExternalResponse("/auth", 200, "ERROR: вход отклонен внешним сервисом");
    responses.ok(loginApi.login(token));
  }

  @Test
  @DisplayName("[XX-017][API] Ошибка внешнего действия не завершает сессию пользователя")
  void failedActionPreservesSession() {
    external.stubExternalResponse("/doAction", 503, "Сервис временно недоступен");
    responses.error(endpoint.action(token));
    external.stubExternalResponse("/doAction", 200, "Сервис восстановлен");
    responses.ok(endpoint.action(token));
    external.verifyCalls("/auth", token, 1);
    external.verifyCalls("/doAction", token, 2);
  }
}
