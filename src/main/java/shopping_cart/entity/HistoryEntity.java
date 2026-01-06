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
  private String id;
  private String userId;
  private String basketId;
  private String basketName;
  private Double totalSpent;
  private String currency;
  private LocalDateTime closedAt;

  private List<HistoryItemEntity> items;
}
