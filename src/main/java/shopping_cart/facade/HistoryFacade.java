package shopping_cart.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;
import shopping_cart.service.BasketService;
import shopping_cart.service.HistoryService;
import shopping_cart.service.SessionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HistoryFacade {

  private final HistoryService historyService;
  private final BasketService basketService;
  private final SessionManager sessionManager;

  public List<HistoryEntity> getHistory(String sessionId) {
    Session session = sessionManager.getSession(sessionId);
    return historyService.getUserHistory(session.getUserId());
  }

  @Transactional
  public void checkout(String sessionId) {
    Session session = sessionManager.getSession(sessionId);
    if (session.getCartId() == null) {
      throw new RuntimeException("User does not have an active basket to checkout.");
    }

    String basketId = session.getCartId().toString();

    // Get only CHECKED items for partial checkout
    List<BasketItemEntity> checkedItems = basketService.findCheckedItemsByBasketId(basketId);
    if (checkedItems.isEmpty()) {
      throw new RuntimeException("No items selected for checkout. Please check at least one item.");
    }

    BigDecimal totalSpent =
          checkedItems.stream()
                  .map(
                          item ->
                                  BigDecimal.valueOf(item.getPrice().doubleValue())
                                          .multiply(BigDecimal.valueOf(item.getQuantity())))
                  .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Archive only checked items
    historyService.archiveCheckedItems(session.getUserId(), basketId, checkedItems, totalSpent);

    // Remove only checked items from basket
    basketService.removeCheckedItems(basketId);

    // Check if basket still has unchecked items
    List<BasketItemEntity> remainingItems = basketService.findItemsByBasketId(basketId);
    if (remainingItems.isEmpty()) {
    sessionManager.removeCartIdAfterCheckout(sessionId);
    }
  }
}
