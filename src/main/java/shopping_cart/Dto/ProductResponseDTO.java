package shopping_cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private String productId;
    private String priceId;
    private String name;
    private String storeName;
    private Double price;
    private String currency;
    private Integer quantity;
    private Double totalLinePrice;
}