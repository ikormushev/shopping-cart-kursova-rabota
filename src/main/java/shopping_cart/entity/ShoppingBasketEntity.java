package shopping_cart.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ShoppingBasketEntity {
  private String id;
  private String userId;
  private String name;
  private Boolean isShared;
  private String ownerId;
  private String shareCode;
  private List<String> members;
  private LocalDateTime createdAt;
}
