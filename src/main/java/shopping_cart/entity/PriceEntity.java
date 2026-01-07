package shopping_cart.entity;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntity {
  private String id;
  private String productId;
  private String storeId;
  private BigDecimal price;
  private String currency;
  private String storeName;
  private OffsetDateTime createdAt;
}
