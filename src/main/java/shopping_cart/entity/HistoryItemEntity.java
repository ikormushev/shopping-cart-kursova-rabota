package shopping_cart.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryItemEntity {
  private String id;
  private String historyId;
  private String productName;
  private Integer quantity;
  private Double priceAtPurchase;
}
