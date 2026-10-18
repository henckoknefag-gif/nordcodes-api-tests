package nordcodes.qa.api;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.Map;
import nordcodes.qa.client.ApiResponse;

@CheckReturnValue
public final class LoginApi {
  private final EndpointApi endpoint;

  public LoginApi(EndpointApi endpoint) {
    this.endpoint = endpoint;
  }

  public ApiResponse login(String token) {
    return endpoint.perform(token, "LOGIN");
  }

  public ApiResponse login(String token, String suppliedKey) {
    return endpoint.send(Map.of("token", token, "action", "LOGIN"), suppliedKey);
  }
}
