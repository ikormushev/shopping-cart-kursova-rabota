package shopping_cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorePriceCalculation {
  private String storeId;
  private String retailerName;
  private Double totalCost;
}
