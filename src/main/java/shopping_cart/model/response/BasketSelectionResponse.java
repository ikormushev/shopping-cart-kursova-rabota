package shopping_cart.model.response;

import lombok.Builder;
import lombok.Data;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.model.domain.ShoppingBasketDto;

import java.util.List;

@Data
@Builder
public class BasketSelectionResponse {
  private String message;
  private ShoppingBasketDto currentBasket;
  private List<HistoryEntity> history;
}
