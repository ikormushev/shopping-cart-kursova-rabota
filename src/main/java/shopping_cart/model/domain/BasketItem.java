package shopping_cart.model.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BasketItem {
  private final UUID productId;
  private final String productName;
  private final int quantity;
  private final Double price;
}
