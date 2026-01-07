package shopping_cart.model.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import shopping_cart.dto.PriceComparisonDto;

@Data
@Builder
public class BasketItem {
  private final String productId;
  private final String productName;
  private final int quantity;
  private final String storeName;
  private final BigDecimal price;
  private final PriceComparisonDto lowerPriceItem;
}
