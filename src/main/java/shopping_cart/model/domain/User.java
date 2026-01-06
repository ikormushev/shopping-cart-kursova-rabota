package shopping_cart.model.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class User {
  private UUID id;
  private String username;
  private String email;
  private String rawPassword;
  private String location;
}
