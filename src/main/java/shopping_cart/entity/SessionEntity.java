package shopping_cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {
  private String sessionId;
  private String status;
  private String cartId;
  private String userId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
