package shopping_cart.entity;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreEntity {
    private String id;
    private String name;
    private String websiteUrl;
    private OffsetDateTime createdAt;
}
