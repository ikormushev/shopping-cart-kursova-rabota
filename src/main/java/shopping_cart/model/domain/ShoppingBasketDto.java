package shopping_cart.model.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShoppingBasketDto {
  private final String id;
  private final String name;
  private final UUID ownerId;
  private final boolean shared;
  private final String shareCode;
  private final List<BasketItem> items;
  private List<String> members;
  private final BigDecimal totalFromSuggestions;
  private final BigDecimal total;
}
