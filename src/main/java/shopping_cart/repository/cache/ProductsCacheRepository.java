package shopping_cart.repository.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import shopping_cart.entity.ProductEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class ProductsCacheRepository {
  private final Map<UUID, ProductEntity> productCache = new ConcurrentHashMap<>();

  private final Map<String, UUID> skuToIndex = new ConcurrentHashMap<>();

  private static final int MAX_CACHE_SIZE = 1000;

  public void put(ProductEntity product) {
    if (productCache.size() >= MAX_CACHE_SIZE) {
      log.warn("Cache full, clearing to prevent memory overflow");
      clear();
    }
    productCache.put(product.getId(), product);
    skuToIndex.put(product.getSku(), product.getId());
  }

  public ProductEntity getById(UUID id) {
    return productCache.get(id);
  }

  public ProductEntity getBySku(String sku) {
    UUID id = skuToIndex.get(sku);
    return (id != null) ? productCache.get(id) : null;
  }

  public void evict(UUID id) {
    ProductEntity p = productCache.remove(id);
    if (p != null) {
      skuToIndex.remove(p.getSku());
    }
  }

  public void clear() {
    productCache.clear();
    skuToIndex.clear();
  }
}
