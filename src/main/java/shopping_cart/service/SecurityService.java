package shopping_cart.service;

import org.springframework.stereotype.Service;

@Service
public class SecurityService {

  public String generateSecureToken() {
    return java.util.UUID.randomUUID().toString();
  }

  public void validatePasswords(String password, String confirmPassword) {
    if (password == null || !password.equals(confirmPassword)) {
      throw new IllegalArgumentException("Passwords do not match!");
    }
  }
}
