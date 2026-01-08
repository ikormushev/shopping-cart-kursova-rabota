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

  @Transactional
  public void checkout(String sessionId) {
    Session session = sessionManager.getSession(sessionId);
    if (session.getCartId() == null) {
      throw new RuntimeException("User does not have an active basket to checkout.");
    }

    String basketId = session.getCartId().toString();

    List<BasketItemEntity> currentItems = basketService.findItemsByBasketId(basketId);
    if (currentItems.isEmpty()) {
      throw new RuntimeException("Cannot checkout an empty basket.");
    }

    BigDecimal totalSpent =
        currentItems.stream()
            .map(
                item ->
                    BigDecimal.valueOf(item.getPrice().doubleValue())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    historyService.archiveBasket(session.getUserId(), basketId, totalSpent);
    sessionManager.update(UUID.fromString(sessionId), session);
    sessionManager.removeCartIdAfterCheckout(sessionId);
  }

  public List<HistoryEntity> getHistory(String sessionId) {
    Session session = sessionManager.getSession(sessionId);
    return historyService.getUserHistory(session.getUserId());
  }
}
