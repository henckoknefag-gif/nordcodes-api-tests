package nordcodes.qa.api.tests.negative;

import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[API] Отказ в действии и неудачный вход")
class SessionErrorTest extends BaseApiTest {

  @Test
  @DisplayName("[XX-002][API] Действие без предварительного входа запрещено")
  void actionRequiresLogin() {
    responses.error(endpoint.action(token));
    external.verifyNoCalls();
  }

  @Test
  @DisplayName("[XX-003][API] Неудачный вход не создает сессию")
  void failedLoginDoesNotCreateSession() {
    external.stubExternalResponse("/auth", 503, "Сервис временно недоступен");
    responses.error(loginApi.login(token));
    responses.error(endpoint.action(token));
    external.verifyCalls("/auth", token, 1);
    external.verifyCalls("/doAction", token, 0);
  }
}
