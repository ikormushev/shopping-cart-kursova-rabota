package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.mapper.ShoppingHistoryMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryService {
  private final ShoppingHistoryMapper historyMapper;

  @Transactional
  public void archiveBasket(
      UUID userId, String basketName, UUID basketId, List<BasketItemEntity> liveItems) {
    UUID historyId = UUID.randomUUID();
    BigDecimal total = BigDecimal.ZERO;

    historyMapper.insertHistoryHeader(historyId, userId, basketName, total, "BGN");

    for (BasketItemEntity item : liveItems) {
      historyMapper.insertHistoryItem(
          historyId, "Product Name Placeholder", item.getQuantity(), BigDecimal.ZERO);
    }
  }
}
