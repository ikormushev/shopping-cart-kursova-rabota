package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.PriceEntity;
import shopping_cart.entity.ProductEntity;
import shopping_cart.mapper.PriceMapper;
import shopping_cart.mapper.ProductMapper;
import shopping_cart.repository.cache.ProductsCacheRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final PriceMapper priceMapper;
    private final ProductsCacheRepository productCache;

    public void saveProduct(ProductEntity product) {
        productMapper.insert(product);
        productCache.put(product);
    }

    public ProductEntity getProductBySku(String sku) {
        ProductEntity cached = productCache.getBySku(sku);
        if (cached != null) return cached;

        ProductEntity fromDb = productMapper.findBySku(sku);
        if (fromDb != null) productCache.put(fromDb);
        return fromDb;
    }

    public List<PriceEntity> getProductPrices(UUID productId) {
        return priceMapper.getAll().stream()
                .filter(p -> p.getProductId().equals(productId))
                .toList();
    }
}
