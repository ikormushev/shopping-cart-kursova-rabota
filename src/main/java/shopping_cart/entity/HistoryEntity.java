package shopping_cart.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@ToString
public class HistoryEntity {
  private UUID id;
  private UUID userId;
  private UUID basketId;
  private String basketName;
  private BigDecimal totalSpent;
  private String currency;
  private LocalDateTime closedAt;

  private List<HistoryItemEntity> items;
}
