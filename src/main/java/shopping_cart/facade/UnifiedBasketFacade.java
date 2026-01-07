package shopping_cart.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.model.domain.BasketItem;
import shopping_cart.model.domain.ShoppingBasketDto;
import shopping_cart.model.response.BasketSelectionResponse;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;
import shopping_cart.service.BasketService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnifiedBasketFacade {
  private final BasketService basketService;
  private final SessionCacheRepository sessionCache;
  private final HistoryFacade historyFacade;

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
  public ShoppingBasketDto addItem(
      String sessionId, String productId, String cartId, int quantity) {
    var session = getSession(sessionId);
    if (!session.getCartId().toString().equalsIgnoreCase(cartId)) {
      session.setCartId(UUID.fromString(cartId));
      sessionCache.update(UUID.fromString(sessionId), session);
    }
    String basketId = session.getCartId().toString();

    validateMembership(basketId, session.getUserId());

    BasketItemEntity item =
        BasketItemEntity.builder()
            .id(UUID.randomUUID().toString())
            .basketId(basketId)
            .productId(productId)
            .quantity(quantity)
            .addedBy(session.getUserId())
            .addedAt(LocalDateTime.now())
            .build();

    basketService.addProductToBasket(item);
    return getBasketDto(basketId);
  }

  private ShoppingBasketDto getBasketDto(String basketId) {
    ShoppingBasketEntity basket = basketService.getBasket(basketId);
    if (basket == null) throw new RuntimeException("Basket not found");

    List<BasketItemEntity> itemEntities = basketService.findItemsByBasketId(basketId);

    List<BasketItem> items =
        itemEntities.stream()
            .map(
                entity ->
                    BasketItem.builder()
                        .productId(entity.getProductId())
                        .productName(entity.getRawName())
                        .price(entity.getPrice())
                        .quantity(entity.getQuantity())
                        .storeName(entity.getStoreName())
                        .build())
            .toList();

    BigDecimal total =
        items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return ShoppingBasketDto.builder()
        .id(basket.getId())
        .name(basket.getName())
        .ownerId(UUID.fromString(basket.getOwnerId()))
        .shared(basket.getShareCode() != null)
        .shareCode(basket.getShareCode())
        .items(items)
        .total(total)
        .build();
  }

  public ShoppingBasketDto removeItem(String sessionId, String productId) {
    var session = getSession(sessionId);
    String basketId = session.getCartId().toString();

    validateMembership(basketId, session.getUserId());
    basketService.removeItemFromBasket(basketId, productId);

    return getBasketDto(basketId);
  }

  @Transactional
  public ShoppingBasketDto updateQuantity(String sessionId, String basketItemId, int quantity) {
    var session = getSession(sessionId);
    BasketItemEntity item =
        basketService.findItemsByBasketId(session.getCartId().toString(), basketItemId);
    if (item == null) throw new RuntimeException("Item not found");

    validateMembership(item.getBasketId(), session.getUserId());
    if (quantity <= 0) {
      basketService.removeItemFromBasket(item.getBasketId(), item.getProductId());
    } else {
      basketService.updateItemQuantity(basketItemId, quantity);
    }

    return getBasketDto(item.getBasketId());
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

  public List<ShoppingBasketEntity> getAllCarts(String sessionId) {
    var session = getSession(sessionId);
    return basketService.findAllForUser(session.getUserId());
  }

  @Transactional
  public BasketSelectionResponse selectBasket(String sessionId, String basketId) {
    var session = getSession(sessionId);

    // 1. Сменяме активната количка
    session.setCartId(UUID.fromString(basketId));
    sessionCache.update(UUID.fromString(sessionId), session);

    // 2. Вземаме текущите продукти
    var currentBasket = getBasketDto(basketId);

    // 3. ПРЕИЗПОЛЗВАМЕ логиката от HistoryFacade за историята
    var history = historyFacade.getHistory(sessionId);

    return BasketSelectionResponse.builder()
        .message("Basket selected")
        .currentBasket(currentBasket)
        .history(history)
        .build();
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
