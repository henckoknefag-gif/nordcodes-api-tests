package nordcodes.qa.api.tests.negative;

import java.util.Map;
import java.util.stream.Stream;
import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("[API] Отказы при неверном API-ключе, токене или действии")
class AccessAndValidationTest extends BaseApiTest {

  /**
   * Подготавливает варианты отсутствующего, пустого и неверного API-ключа.
   *
   * @return варианты описания проверки и API-ключа
   */
  static Stream<Arguments> invalidKeys() {
    return Stream.of(
        Arguments.of("=> без API-ключа доступ запрещен", null),
        Arguments.of("=> пустой API-ключ не дает доступ", ""),
        Arguments.of("=> неверный API-ключ не дает доступ", "incorrect-demo-key"));
  }

  /**
   * Подготавливает токены неверной длины и токен со строчными буквами.
   *
   * @return варианты описания проверки и некорректного токена
   */
  static Stream<Arguments> invalidTokens() {
    return Stream.of(
        Arguments.of("=> токен из 31 символа отклоняется", "A".repeat(31)),
        Arguments.of("=> токен из 33 символов отклоняется", "A".repeat(33)),
        Arguments.of("=> строчные буквы в токене запрещены", "a".repeat(32)));
  }

  @ParameterizedTest(name = "{displayName} {0}")
  @MethodSource("invalidKeys")
  @DisplayName("[XX-006][XX-007][XX-008][API] Доступ по API-ключу")
  void invalidKeyIsRejected(String scenario, String key) {
    responses.error(loginApi.login(token, key));
    external.verifyNoCalls();

    responses.ok(loginApi.login(token));
    responses.error(endpoint.send(Map.of("token", token, "action", "ACTION"), key));
    responses.error(endpoint.send(Map.of("token", token, "action", "LOGOUT"), key));
    external.verifyCalls("/auth", token, 1);
    external.verifyCalls("/doAction", token, 0);

    responses.ok(endpoint.action(token));
    external.verifyCalls("/doAction", token, 1);
  }

  @ParameterizedTest(name = "{displayName} {0}")
  @MethodSource("invalidTokens")
  @DisplayName("[XX-009][XX-010][XX-011][API] Проверка формата токена")
  void invalidTokenIsRejected(String scenario, String invalidToken) {
    responses.error(loginApi.login(invalidToken));
    external.verifyNoCalls();
  }

  @Test
  @DisplayName("[XX-012][API] Запрос без токена отклоняется")
  void missingTokenIsRejected() {
    responses.error(endpoint.send(Map.of("action", "LOGIN"), API_KEY));
    external.verifyNoCalls();
  }

  @Test
  @DisplayName("[XX-013][API] Запрос без действия отклоняется")
  void missingActionIsRejected() {
    responses.error(endpoint.send(Map.of("token", token), API_KEY));
    external.verifyNoCalls();
  }

  @Test
  @DisplayName("[XX-014][API] Неизвестное действие отклоняется")
  void unknownActionIsRejected() {
    responses.error(endpoint.perform(token, "DELETE"));
    external.verifyNoCalls();
  }
}
