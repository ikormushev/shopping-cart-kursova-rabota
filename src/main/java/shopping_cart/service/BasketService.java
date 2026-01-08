package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shopping_cart.dto.PriceComparisonDto;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.BasketMemberEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.mapper.BasketMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BasketService {
  private final BasketMapper basketMapper;

  public ShoppingBasketEntity getBasket(String id) {
    return basketMapper.findById(id);
  }

  public void createBasket(ShoppingBasketEntity basket) {
    basketMapper.createBasket(basket);
    basketMapper.addMember(new BasketMemberEntity(basket.getId(), basket.getOwnerId(), "OWNER"));
  }

  public void addCollaborator(String basketId, String userId) {
    basketMapper.addMember(new BasketMemberEntity(basketId, userId, "CONTRIBUTOR"));
  }

  public List<String> getMemberUsernames(String basketId) {
    return basketMapper.getMemberUsernames(basketId);
  }

  public PriceComparisonDto findCheaperOption(String rawName, BigDecimal price, String productId) {
    return basketMapper.findCheaperOption(rawName, price, productId);
  }

  public void addProductToBasket(BasketItemEntity item) {
    basketMapper.addItem(item);
  }

  public void updateItemQuantity(String itemId, int quantity) {
    basketMapper.updateQuantity(itemId, quantity);
  }

  public void removeItemFromBasket(String basketId, String productId) {
    basketMapper.removeItem(basketId, productId);
  }

  public String getIdBySharedCode(String sharedCode) {
    return basketMapper.findBasketIdByShareCode(sharedCode);
  }

  public List<BasketItemEntity> findItemsByBasketId(String basketId) {
    return basketMapper.findItemsByBasketId(basketId);
  }

  public BasketItemEntity findItemsByBasketId(String basketId, String productId) {
    return basketMapper.findItemById(basketId, productId);
  }

  public List<ShoppingBasketEntity> findAllForUser(String userId) {
    return basketMapper.findAllByUserId(userId);
  }

  public void addProductsToBasketBatch(List<BasketItemEntity> items) {
    if (items != null && !items.isEmpty()) {
      basketMapper.addItemsBatch(items);
    }
  }

  public String getUserRole(String basketId, String userId) {
    return basketMapper.getUserRole(basketId, userId);
  }
}
