package shopping_cart.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketItemEntity {
  private String id;
  private String basketId;
  private String productId;
  private Integer quantity;
  private String addedBy;
  private LocalDateTime addedAt;

  private String rawName;
  private BigDecimal price;
  private String storeName;
}
