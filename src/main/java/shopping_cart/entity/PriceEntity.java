package shopping_cart.entity;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntity {
    private UUID id;
    private UUID productId;
    private UUID storeId;
    private BigDecimal price;
    private String currency;
    private OffsetDateTime createdAt;
}