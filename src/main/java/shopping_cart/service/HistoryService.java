package shopping_cart.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.mapper.ShoppingHistoryMapper;

@Service
@RequiredArgsConstructor
public class HistoryService {
  private final BasketMapper basketMapper;
  private final ShoppingHistoryMapper shoppingHistoryMapper;

  @Transactional
  public void archiveBasket(String userId, String basketId, BigDecimal total) {
    String historyId = UUID.randomUUID().toString();

    var basket = basketMapper.findById(basketId);
    if (basket == null) throw new RuntimeException("Basket not found");

    HistoryEntity history =
        HistoryEntity.builder()
            .id(historyId)
            .userId(userId)
            .basketId(basketId)
            .basketName(basket.getName())
            .totalSpent(total.doubleValue())
            .currency("BGN")
            .closedAt(LocalDateTime.now())
            .build();

    shoppingHistoryMapper.insertHistoryHeader(history);

    shoppingHistoryMapper.snapshotBasketToHistory(historyId, basketId);

    basketMapper.clearBasket(basketId);
  }

    public List<HistoryEntity> getUserHistory(String userId) {
        return shoppingHistoryMapper.getFullHistoryByUserId(userId);
    }

    public List<HistoryEntity> getLastOrders(String userId, int limit) {
        List<HistoryEntity> allHistory = shoppingHistoryMapper.getFullHistoryByUserId(userId);
        return allHistory.stream().limit(limit).toList();
    }

}
