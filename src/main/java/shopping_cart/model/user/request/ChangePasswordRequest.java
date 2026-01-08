package shopping_cart.model.user.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
  private String email;
  private String oldPassword;
  private String newPassword;
  private String confirmPassword;
}
