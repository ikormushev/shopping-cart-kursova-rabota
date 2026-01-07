package shopping_cart.model.session;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Session {
  private final UUID sessionId;
  private String userId;
  private UUID cartId;
  private String status;
}
