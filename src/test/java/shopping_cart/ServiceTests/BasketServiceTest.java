package shopping_cart.ServiceTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.BasketMemberEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.service.BasketService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketMapper basketMapper;

    @InjectMocks
    private BasketService basketService;


    @Test
    @DisplayName("createBasket: Should create basket AND add user as OWNER")
    void createBasket() {
        ShoppingBasketEntity basket = new ShoppingBasketEntity();
        basket.setId("basket-123");
        basket.setOwnerId("user-999");

        basketService.createBasket(basket);

        verify(basketMapper).createBasket(basket);

        ArgumentCaptor<BasketMemberEntity> memberCaptor = ArgumentCaptor.forClass(BasketMemberEntity.class);
        verify(basketMapper).addMember(memberCaptor.capture());

        BasketMemberEntity capturedMember = memberCaptor.getValue();

        assertEquals("basket-123", capturedMember.getBasketId()); // приемаме, че има getter
        assertEquals("user-999", capturedMember.getUserId());     // приемаме, че има getter
        assertEquals("OWNER", capturedMember.getRole());
    }

    @Test
    @DisplayName("addCollaborator: Should add member with CONTRIBUTOR role")
    void addCollaborator() {
        String basketId = "basket-1";
        String userId = "user-2";

        basketService.addCollaborator(basketId, userId);

        ArgumentCaptor<BasketMemberEntity> memberCaptor = ArgumentCaptor.forClass(BasketMemberEntity.class);
        verify(basketMapper).addMember(memberCaptor.capture());

        BasketMemberEntity capturedMember = memberCaptor.getValue();
        assertEquals("CONTRIBUTOR", capturedMember.getRole());
        assertEquals(basketId, capturedMember.getBasketId());
        assertEquals(userId, capturedMember.getUserId());
    }

    @Test
    @DisplayName("getBasket: Should return basket by ID")
    void getBasket() {
        String id = "basket-1";
        ShoppingBasketEntity expectedBasket = new ShoppingBasketEntity();
        expectedBasket.setId(id);

        when(basketMapper.findById(id)).thenReturn(expectedBasket);

        ShoppingBasketEntity result = basketService.getBasket(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("getIdBySharedCode: Should return basket ID")
    void getIdBySharedCode() {
        String code = "SHARE-CODE-XYZ";
        when(basketMapper.findBasketIdByShareCode(code)).thenReturn("basket-found-id");

        String result = basketService.getIdBySharedCode(code);

        assertEquals("basket-found-id", result);
    }

    @Test
    @DisplayName("findAllForUser: Should return list of baskets")
    void findAllForUser() {
        String userId = "u1";
        when(basketMapper.findAllByUserId(userId)).thenReturn(Arrays.asList(new ShoppingBasketEntity(), new ShoppingBasketEntity()));

        List<ShoppingBasketEntity> result = basketService.findAllForUser(userId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("addProductToBasket: Should call mapper to add item")
    void addProductToBasket() {
        BasketItemEntity item = new BasketItemEntity();
        basketService.addProductToBasket(item);
        verify(basketMapper).addItem(item);
    }

    @Test
    @DisplayName("updateItemQuantity: Should call mapper to update quantity")
    void updateItemQuantity() {
        basketService.updateItemQuantity("item-1", 5);
        verify(basketMapper).updateQuantity("item-1", 5);
    }

    @Test
    @DisplayName("removeItemFromBasket: Should call mapper to remove item")
    void removeItemFromBasket() {
        basketService.removeItemFromBasket("basket-1", "product-1");
        verify(basketMapper).removeItem("basket-1", "product-1");
    }

    @Test
    @DisplayName("findItemsByBasketId (List): Should return items list")
    void findItemsByBasketId_List() {
        String basketId = "b1";
        when(basketMapper.findItemsByBasketId(basketId)).thenReturn(Collections.singletonList(new BasketItemEntity()));

        List<BasketItemEntity> items = basketService.findItemsByBasketId(basketId);

        assertEquals(1, items.size());
    }

    @Test
    @DisplayName("findItemsByBasketId (Single): Should return specific item")
    void findItemsByBasketId_Single() {
        String basketId = "b1";
        String productId = "p1";
        BasketItemEntity item = new BasketItemEntity();

        when(basketMapper.findItemById(basketId, productId)).thenReturn(item);

        BasketItemEntity result = basketService.findItemsByBasketId(basketId, productId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getUserRole: Should return role string")
    void getUserRole() {
        String basketId = "b1";
        String userId = "u1";
        when(basketMapper.getUserRole(basketId, userId)).thenReturn("OWNER");

        String role = basketService.getUserRole(basketId, userId);

        assertEquals("OWNER", role);
    }
}