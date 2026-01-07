package shopping_cart.model.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BasketItem {
  private final String productId;
  private final String productName;
  private final int quantity;
  private final String storeName;
  private final BigDecimal price;
}
