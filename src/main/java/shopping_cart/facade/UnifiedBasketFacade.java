package shopping_cart.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.entity.StorePriceCalculation;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;
import shopping_cart.service.BasketService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnifiedBasketFacade {
  private final BasketService basketService;
  private final SessionCacheRepository sessionCache;

  @Transactional
  public void createCart(String sessionId, String name) {
    var session = getSession(sessionId);
    String basketId = UUID.randomUUID().toString();

    ShoppingBasketEntity basket =
        ShoppingBasketEntity.builder()
            .id(basketId)
            .name(name)
            .ownerId(session.getUserId())
            .shareCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .createdAt(LocalDateTime.now())
            .build();

    basketService.createBasket(basket);

    session.setCartId(UUID.fromString(basketId));
    sessionCache.update(UUID.fromString(sessionId), session);
  }

  @Transactional
  public void addItem(String sessionId, String productId, int quantity) {
    var session = getSession(sessionId);
    String basketId = session.getCartId().toString();

    validateMembership(basketId, session.getUserId());

    BasketItemEntity item =
        BasketItemEntity.builder()
            .id(UUID.randomUUID().toString())
            .basketId(basketId)
            .productId(productId)
            .quantity(quantity)
            .addedBy(session.getUserId())
            .createdAt(OffsetDateTime.now())
            .build();

    basketService.addProductToBasket(item);
  }

  @Transactional
  public void removeItem(String sessionId, String productId) {
    var session = getSession(sessionId);
    String basketId = session.getCartId().toString();

    validateMembership(basketId, session.getUserId());

    basketService.removeItemFromBasket(basketId, productId);
  }

  @Transactional
  public void updateQuantity(String sessionId, String basketItemId, int quantity) {
    var session = getSession(sessionId);
    basketService.updateItemQuantity(basketItemId, quantity);
  }

  @Transactional
  public void joinCartViaCode(String sessionId, String shareCode) {
    var session = getSession(sessionId);
    String basketId = basketService.getIdBySharedCode(shareCode);

    if (basketId == null) throw new RuntimeException("Invalid share code.");

    basketService.addCollaborator(basketId, session.getUserId());

    session.setCartId(UUID.fromString(basketId));
    sessionCache.update(UUID.fromString(sessionId), session);
  }

  public List<BasketItemEntity> getCurrentOrder(String sessionId) {
    var session = getSession(sessionId);
    return basketService.findItemsByBasketId(session.getCartId().toString());
  }

  private void validateMembership(String basketId, String userId) {
    String role = basketService.getUserRole(basketId, userId);
    if (role == null) throw new RuntimeException("You are not a member of this cart!");
  }

  private Session getSession(String sessionId) {
    var session = sessionCache.get(UUID.fromString(sessionId));
    if (session == null) throw new RuntimeException("Invalid Session");
    return session;
  }
}
