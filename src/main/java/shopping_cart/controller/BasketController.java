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

    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@RequestHeader("Session-Id") String sessionId,
                                        @RequestParam String priceId,
                                        @RequestParam int quantity) {
        basketFacade.addItem(sessionId, priceId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current")
    public ResponseEntity<List<BasketItemEntity>> getCurrent(@RequestHeader("Session-Id") String sessionId) {
        return ResponseEntity.ok(basketFacade.getCurrentOrder(sessionId));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> removeItem(@RequestHeader("Session-Id") String sessionId, 
                                         @PathVariable String itemId) {
        basketFacade.removeItem(sessionId, itemId);
        return ResponseEntity.ok().build();
    }
}