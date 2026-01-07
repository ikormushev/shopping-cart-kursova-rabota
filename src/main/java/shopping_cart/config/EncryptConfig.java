package shopping_cart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
@RequiredArgsConstructor
public class EncryptConfig {
  private final AppConfig appConfig;

  @Bean
  public TextEncryptor getTextEncryptor() {
    final String secreteKey = appConfig.getSecreteKey();
    final String secreteSalt = appConfig.getSecretSalt();
    return Encryptors.text(secreteKey, secreteSalt);
  }
}
