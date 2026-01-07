package shopping_cart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroupEntity {
  private String id;
  private String canonicalName;
  private String category;
  private String imageUrl;
}
