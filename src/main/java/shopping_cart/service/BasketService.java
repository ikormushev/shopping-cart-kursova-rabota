package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.entity.StorePriceCalculation;
import shopping_cart.mapper.BasketMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasketService {
    private final BasketMapper basketMapper;

    public ShoppingBasketEntity getBasket(String id) {
        return basketMapper.getById(id);
    }

    public void createBasket(ShoppingBasketEntity basket) {
        if (basket.getId() == null) {
            basket.setId(UUID.randomUUID().toString());
        }
        basketMapper.insert(basket);
    }

    public StorePriceCalculation calculateCheapestStore(String basketId) {
        return basketMapper.getCheapestStore(basketId);
    }
}