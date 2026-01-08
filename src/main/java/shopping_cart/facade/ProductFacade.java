package shopping_cart.facade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shopping_cart.dto.ProductResponseDTO;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.ProductEntity;
import shopping_cart.entity.ProductGroupEntity;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.mapper.ProductMapper;

@Component
@RequiredArgsConstructor
public class ProductFacade {
  private final ProductMapper productMapper;

  public List<ProductGroupEntity> getAllCategories() {
    return productMapper.findAllGroups();
  }

  public List<ProductResponseDTO> getStoreOffers(String groupId) {
    List<ProductEntity> variants = productMapper.findByGroupId(groupId);
    return variants.stream().map(this::mapToResponseDTO).toList();
  }


  public List<ProductResponseDTO> getAllAvailableProducts() {
    return productMapper.findAllProducts().stream().map(this::mapToResponseDTO).toList();
  }

  public List<ProductResponseDTO> searchProducts() {
    return productMapper.searchProductsByName().stream().map(this::mapToResponseDTO).toList();
  }

  public ProductResponseDTO getProductById(String productId) {
    ProductEntity entity = productMapper.findById(productId);
    if (entity == null) {
      throw new RuntimeException("Product not found with ID: " + productId);
    }
    return mapToResponseDTO(entity);
  }


  private ProductResponseDTO mapToResponseDTO(ProductEntity v) {
    return ProductResponseDTO.builder()
        .productId(v.getId())
        .name(v.getRawName())
        .storeName(v.getStoreName())
        .price(v.getPrice().doubleValue())
        .currency(v.getCurrency())
        .build();
  }
}
