package shopping_cart.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketMemberEntity {
  private String basketId;
  private String userId;
  private String role;
}
