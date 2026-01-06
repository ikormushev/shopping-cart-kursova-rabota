package shopping_cart.model.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShoppingBasket {
  private final UUID id;
  private final String name;
  private final UUID ownerId;
  private final boolean shared;
  private final String shareCode;
  private final List<BasketItem> items;
}
