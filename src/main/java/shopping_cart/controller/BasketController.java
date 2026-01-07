package shopping_cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.facade.UnifiedBasketFacade;

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
  public ResponseEntity<Void> addItem(
      @RequestHeader("Session-Id") String sessionId,
      @RequestParam String productId,
      @RequestParam int quantity) {
    basketFacade.addItem(sessionId, productId, quantity);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/current")
  public ResponseEntity<List<BasketItemEntity>> getCurrent(
      @RequestHeader("Session-Id") String sessionId) {
    return ResponseEntity.ok(basketFacade.getCurrentOrder(sessionId));
  }

  @PatchMapping("/quantity")
  public ResponseEntity<Void> updateQuantity(
      @RequestHeader("Session-Id") String sessionId,
      @RequestParam String basketItemId,
      @RequestParam int quantity) {
    basketFacade.updateQuantity(sessionId, basketItemId, quantity);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/item/{productId}")
  public ResponseEntity<Void> removeItem(
      @RequestHeader("Session-Id") String sessionId, @PathVariable String productId) {
    basketFacade.removeItem(sessionId, productId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/join")
  public ResponseEntity<String> join(
      @RequestHeader("Session-Id") String sessionId, @RequestParam String code) {
    basketFacade.joinCartViaCode(sessionId, code);
    return ResponseEntity.ok("Successfully joined the cart!");
  }
}
