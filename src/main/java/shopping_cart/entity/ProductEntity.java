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
public class ProductEntity {
  private String id;
  private String groupId;
  private String storeId;
  private String rawName;
  private String sku;
  private BigDecimal price;
  private String currency;
  private String storeName;
  private OffsetDateTime createdAt;
}
