package shopping_cart.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnifiedBasketFacade {
  private final BasketMapper basketMapper;
  private final SessionCacheRepository sessionCache;

  @Transactional
  public void addItem(String sessionId, String priceId, int quantity) {
    var session = getSession(sessionId);
    String internalItemId = UUID.randomUUID().toString();

    basketMapper.addItemByPriceId(
        internalItemId, session.getCartId().toString(), priceId, quantity, session.getUserId());
  }

  public void removeItem(String sessionId, String basketItemId) {
    var session = getSession(sessionId);
    basketMapper.removeItem(session.getCartId().toString(), basketItemId);
  }

  public List<BasketItemEntity> getCurrentOrder(String sessionId) {
    var session = getSession(sessionId);
    return basketMapper.getItemsByBasketId(session.getCartId().toString());
  }

  private Session getSession(String sessionId) {
    var session = sessionCache.get(UUID.fromString(sessionId));
    if (session == null) throw new RuntimeException("Invalid Session");
    return session;
  }
}
