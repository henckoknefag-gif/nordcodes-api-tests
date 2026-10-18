package nordcodes.qa.api.tests;

import io.qameta.allure.Allure;
import java.util.Locale;
import java.util.UUID;
import nordcodes.qa.api.EndpointApi;
import nordcodes.qa.api.LoginApi;
import nordcodes.qa.assertions.ApiAssertions;
import nordcodes.qa.client.ApiClient;
import nordcodes.qa.environment.ExternalServiceMock;
import nordcodes.qa.environment.TestApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiTest {
  protected static final String API_KEY = "qazWSXedc";
  protected final ExternalServiceMock external = new ExternalServiceMock();
  protected final ApiAssertions responses = new ApiAssertions(API_KEY);
  protected EndpointApi endpoint;
  protected LoginApi loginApi;
  protected String token;
  private final TestApplication application = new TestApplication();

  @BeforeAll
  void startStand() throws Exception {
    external.start();
    try {
      application.start(external.baseUrl(), API_KEY);
      endpoint = new EndpointApi(new ApiClient(application.baseUrl()), API_KEY);
      loginApi = new LoginApi(endpoint);
    } catch (Exception error) {
      external.close();
      throw error;
    }
  }

  /**
   * Выделяет новый токен и очищает внешний сервис перед каждой проверкой.
   *
   * @param testInfo сведения о проверке для названия в отчете
   */
  @BeforeEach
  void prepareScenario(TestInfo testInfo) {
    Allure.getLifecycle().updateTestCase(result -> result.setName(testInfo.getDisplayName()));
    token = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    external.reset();
  }

  @AfterAll
  void stopStand() {
    try {
      application.close();
    } finally {
      external.close();
    }
  }
}
