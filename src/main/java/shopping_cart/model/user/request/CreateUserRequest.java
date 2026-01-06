package shopping_cart.model.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class CreateUserRequest {
  @NotBlank private String username;
  @NotBlank private String email;
  @NotBlank private String password;
  @NotBlank private String passwordConfirmation;
  private String location;
}
