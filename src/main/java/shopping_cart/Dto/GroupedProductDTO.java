package shopping_cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shopping_cart.entity.PriceEntity;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedProductDTO {
    private String id;
    private String name;
    private String sku;
    private String category;
    private List<PriceEntity> allOffers;
    private BigDecimal recommendedPrice;
    private String recommendedStoreId;
    private String recommendedStoreName; 
}