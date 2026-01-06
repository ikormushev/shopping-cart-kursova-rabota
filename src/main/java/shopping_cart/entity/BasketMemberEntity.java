package shopping_cart.entity;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class BasketMemberEntity {
    private UUID id;
    private UUID basketId;
    private UUID userId;
    private String role;
    private OffsetDateTime createdAt;
}
