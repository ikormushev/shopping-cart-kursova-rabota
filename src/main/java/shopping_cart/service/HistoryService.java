package shopping_cart.service;

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
  public void archiveBasket(String userId, String basketId, String storeId, Double total) {
    String historyId = UUID.randomUUID().toString();

    var basket = basketMapper.getById(basketId);
    if (basket == null) throw new RuntimeException("Basket not found");

    // 3. Create the History Entity
    HistoryEntity history = new HistoryEntity();
    history.setId(historyId);
    history.setUserId(userId);
    history.setBasketId(basketId);
    history.setBasketName(basket.getName());
    history.setTotalSpent(total);
    history.setCurrency("BGN");

    shoppingHistoryMapper.insertHistoryHeader(history);

    shoppingHistoryMapper.archiveItemsFromActiveBasket(historyId, basketId, storeId);

    basketMapper.clearBasketItems(basketId);
  }
}
