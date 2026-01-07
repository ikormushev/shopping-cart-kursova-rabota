package shopping_cart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/** Note this class will be used to supply env variables from the properties, if needed */
@Service
@RequiredArgsConstructor
public class AppConfig {
  private final Environment environment;

  public String getSecreteKey() {
    return environment.getProperty("security.secret.key");
  }

  public String getSecretSalt() {
    return environment.getProperty("security.secret.salt");
  }
}
