package shopping_cart.entity;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
public class UserEntity {
  private String id;
  private String username;
  private String email;
  private String passwordHash;
  private String location;
  private LocalDateTime createdAt;
}
