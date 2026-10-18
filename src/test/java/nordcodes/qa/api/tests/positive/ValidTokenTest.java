package nordcodes.qa.api.tests.positive;

import java.util.stream.Stream;
import nordcodes.qa.api.tests.BaseApiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("[API] Вход с допустимыми токенами")
class ValidTokenTest extends BaseApiTest {

  /**
   * Подготавливает токены из 32 символов с разрешенными буквами A-Z и цифрами.
   *
   * @return варианты описания проверки и допустимого токена
   */
  static Stream<Arguments> allowedLetters() {
    return Stream.of(
        Arguments.of("=> допустимая буква G не должна мешать входу", "G".repeat(32)),
        Arguments.of(
            "=> токен с буквами A-Z и цифрами должен приниматься",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ012345"));
  }

  @ParameterizedTest(name = "{displayName} {0}")
  @MethodSource("allowedLetters")
  @DisplayName("[XX-015][XX-016][API] Допустимые буквы в токене")
  void allDocumentedLettersAreAccepted(String scenario, String validToken) {
    responses.ok(loginApi.login(validToken));
    external.verifyCalls("/auth", validToken, 1);
    responses.ok(endpoint.action(validToken));
    external.verifyCalls("/doAction", validToken, 1);
  }
}
