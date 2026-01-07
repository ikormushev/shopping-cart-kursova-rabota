package shopping_cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.facade.HistoryFacade;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryFacade historyFacade;
    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestHeader("Session-Id") String sessionId) {
        historyFacade.checkout(sessionId);
        return ResponseEntity.ok("Checkout successful. Your order has been archived.");
    }

    @GetMapping("/all")
    public ResponseEntity<List<HistoryEntity>> getFullHistory(@RequestHeader("Session-Id") String sessionId) {
        List<HistoryEntity> history = historyFacade.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<HistoryEntity>> getRecentOrders(
            @RequestHeader("Session-Id") String sessionId,
            @RequestParam(defaultValue = "3") int limit) {
        List<HistoryEntity> history = historyFacade.getHistory(sessionId);

        List<HistoryEntity> recent = history.stream()
                .limit(limit)
                .toList();
                
        return ResponseEntity.ok(recent);
    }
}