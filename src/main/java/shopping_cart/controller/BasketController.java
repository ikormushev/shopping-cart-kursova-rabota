package shopping_cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.facade.UnifiedBasketFacade;
import shopping_cart.model.domain.ShoppingBasketDto;
import shopping_cart.model.response.BasketSelectionResponse;
import shopping_cart.model.user.request.AddItemRequest;

import java.util.List;

@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
public class BasketController {
  private final UnifiedBasketFacade basketFacade;

  @PostMapping("/create")
  public ResponseEntity<Void> createBasket(
      @RequestHeader("Session-Id") String sessionId, @RequestParam String name) {
    basketFacade.createCart(sessionId, name);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/add")
  public ResponseEntity<ShoppingBasketDto> addItem(
      @RequestHeader("Session-Id") String sessionId,
      @RequestBody List<AddItemRequest> itemRequests,
      @RequestParam String cartId) {
    return ResponseEntity.ok(basketFacade.addItemsBatch(sessionId, cartId, itemRequests));
  }

  @GetMapping("/current")
  public ResponseEntity<List<BasketItemEntity>> getCurrent(
      @RequestHeader("Session-Id") String sessionId) {
    return ResponseEntity.ok(basketFacade.getCurrentOrder(sessionId));
  }

  @GetMapping("/get/user/carts")
  public ResponseEntity<List<ShoppingBasketEntity>> getUserCarts(
      @RequestHeader("Session-Id") String sessionId) {
    return ResponseEntity.ok(basketFacade.getAllCarts(sessionId));
  }

  @PostMapping("/select/cart")
  public ResponseEntity<BasketSelectionResponse> selectCarts(
      @RequestHeader("Session-Id") String sessionId, @RequestParam String basketId) {
    return ResponseEntity.ok(basketFacade.selectBasket(sessionId, basketId));
  }

  @PatchMapping("/quantity")
  public ResponseEntity<ShoppingBasketDto> updateQuantity(
      @RequestHeader("Session-Id") String sessionId,
      @RequestParam String basketItemId,
      @RequestParam int quantity) {
    return ResponseEntity.ok(basketFacade.updateQuantity(sessionId, basketItemId, quantity));
  }

  @DeleteMapping("/item/{productId}")
  public ResponseEntity<ShoppingBasketDto> removeItem(
      @RequestHeader("Session-Id") String sessionId, @PathVariable String productId) {
    basketFacade.removeItem(sessionId, productId);
    return ResponseEntity.ok(basketFacade.removeItem(sessionId, productId));
  }

  @PostMapping("/join")
  public ResponseEntity<String> join(
      @RequestHeader("Session-Id") String sessionId, @RequestParam String code) {
    basketFacade.joinCartViaCode(sessionId, code);
    return ResponseEntity.ok("Successfully joined the cart!");
  }
}
