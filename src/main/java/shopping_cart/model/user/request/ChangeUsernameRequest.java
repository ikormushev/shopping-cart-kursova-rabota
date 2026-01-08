package shopping_cart.model.user.request;

import lombok.Data;

@Data
public class ChangeUsernameRequest {
  private String email;
  private String currentPassword;
  private String newUsername;
}
