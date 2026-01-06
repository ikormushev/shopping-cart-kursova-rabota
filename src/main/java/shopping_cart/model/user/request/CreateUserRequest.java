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
  private String email;
  private String password;
  private String location;
}
