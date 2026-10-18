package nordcodes.qa.api.tests.positive;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import java.util.UUID;
import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[API] Успешный вход, действия и выход пользователя")
class SessionTest extends BaseApiTest {

  @Test
  @DisplayName("[XX-001][API] Пользователь входит, выполняет действие и выходит")
  @Severity(SeverityLevel.CRITICAL)
  void completeUserJourney() {
    token = "01234567890123456789012345678901";
    responses.ok(loginApi.login(token));
    responses.ok(endpoint.action(token));
    external.verifyCalls("/auth", token, 1);
    external.verifyCalls("/doAction", token, 1);

    responses.ok(endpoint.logout(token));
    responses.error(endpoint.action(token));
    external.verifyTotalCalls(2);
  }

  @Test
  @DisplayName("[XX-004][API] Выход одного пользователя не завершает сессию другого")
  void sessionsAreIndependent() {
    String secondToken =
        UUID.randomUUID().toString().replace("-", "").toUpperCase(java.util.Locale.ROOT);
    responses.ok(loginApi.login(token));
    responses.ok(loginApi.login(secondToken));
    responses.ok(endpoint.logout(token));
    responses.error(endpoint.action(token));
    responses.ok(endpoint.action(secondToken));
    external.verifyCalls("/doAction", token, 0);
    external.verifyCalls("/doAction", secondToken, 1);
  }

  @Test
  @DisplayName("[XX-005][API] После выхода можно войти снова с тем же токеном")
  void loginAfterLogout() {
    responses.ok(loginApi.login(token));
    responses.ok(endpoint.logout(token));
    responses.ok(loginApi.login(token));
    responses.ok(endpoint.action(token));
    external.verifyCalls("/auth", token, 2);
    external.verifyCalls("/doAction", token, 1);
  }
}
