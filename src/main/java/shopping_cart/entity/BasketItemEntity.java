package shopping_cart.entity;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketItemEntity {
    private UUID productId;
    private Integer quantity;
    private UUID addedBy;
    private OffsetDateTime createdAt;
}

