package nordcodes.qa.api;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Map;
import nordcodes.qa.client.ApiClient;
import nordcodes.qa.client.ApiResponse;

@CheckReturnValue
public final class EndpointApi {
  private final ApiClient client;
  private final String apiKey;

  public EndpointApi(ApiClient client, String apiKey) {
    this.client = client;
    this.apiKey = apiKey;
  }

  public ApiResponse action(String token) {
    return perform(token, "ACTION");
  }

  public ApiResponse logout(String token) {
    return perform(token, "LOGOUT");
  }

  public ApiResponse perform(String token, String action) {
    return send(Map.of("token", token, "action", action), apiKey);
  }

  public ApiResponse send(Map<String, String> form, String suppliedKey) {
    return client.send(form, suppliedKey);
  }
}
