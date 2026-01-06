package shopping_cart.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShoppingBasketEntity {
  private String id;
  private String userId;
  private String name;
  private Boolean isShared;
  private LocalDateTime createdAt;
}
