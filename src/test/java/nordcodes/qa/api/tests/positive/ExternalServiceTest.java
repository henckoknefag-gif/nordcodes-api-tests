package nordcodes.qa.api.tests.positive;

import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[API] Обработка успешного ответа внешнего сервиса")
class ExternalServiceTest extends BaseApiTest {

  @BeforeEach
  void loginUser() {
    external.stubExternalResponse("/auth", 200, "ERROR: вход отклонен внешним сервисом");
    responses.ok(loginApi.login(token));
  }

  @Test
  @DisplayName("[XX-018][API] Результат зависит от кода внешнего ответа, а не от его текста")
  void externalBodyDoesNotOverrideStatus() {
    external.stubExternalResponse("/doAction", 200, "ERROR: действие отклонено внешним сервисом");
    responses.ok(endpoint.action(token));
    external.verifyCalls("/auth", token, 1);
    external.verifyCalls("/doAction", token, 1);
  }
}
