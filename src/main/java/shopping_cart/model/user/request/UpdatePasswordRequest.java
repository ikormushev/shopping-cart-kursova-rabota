package shopping_cart.model.user.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class UpdatePasswordRequest {
  private String token;
  private String newPassword;
  private String confirmPassword;
}
