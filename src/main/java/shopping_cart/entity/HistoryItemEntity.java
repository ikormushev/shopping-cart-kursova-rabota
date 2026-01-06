package shopping_cart.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
public class HistoryItemEntity {
  private UUID id;
  private UUID historyId;
  private String productName;
  private int quantity;
  private BigDecimal priceAtPurchase;
}
