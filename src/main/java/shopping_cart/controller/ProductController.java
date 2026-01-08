package shopping_cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.dto.ProductResponseDTO;
import shopping_cart.entity.ProductGroupEntity;
import shopping_cart.facade.ProductFacade;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductFacade productFacade;

  @GetMapping("/categories")
  public ResponseEntity<List<ProductGroupEntity>> getCategories() {
    return ResponseEntity.ok(productFacade.getAllCategories());
  }

  @GetMapping("/compare/{groupId}")
  public ResponseEntity<List<ProductResponseDTO>> getStoreOffers(@PathVariable String groupId) {
    return ResponseEntity.ok(productFacade.getStoreOffers(groupId));
  }

  @GetMapping("/search")
  public ResponseEntity<List<ProductResponseDTO>> search(
      @RequestParam(required = false) String query) {
    if (query == null || query.isBlank()) {
      return ResponseEntity.ok(productFacade.getAllAvailableProducts());
    }
    return ResponseEntity.ok(productFacade.searchProducts());
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable String productId) {
    return ResponseEntity.ok(productFacade.getProductById(productId));
  }
}
