package shopping_cart.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class HistoryEntity {
  private String id;
  private String userId;
  private String basketId;
  private String basketName;
  private Double totalSpent;
  private String currency;
  private LocalDateTime closedAt;

  private List<HistoryItemEntity> items;
}
