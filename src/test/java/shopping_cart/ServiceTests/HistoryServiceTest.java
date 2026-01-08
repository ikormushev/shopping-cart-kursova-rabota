package shopping_cart.ServiceTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.mapper.BasketMapper;
import shopping_cart.mapper.ShoppingHistoryMapper;
import shopping_cart.service.HistoryService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private BasketMapper basketMapper;

    @Mock
    private ShoppingHistoryMapper shoppingHistoryMapper;

    @InjectMocks
    private HistoryService historyService;

    @Test
    @DisplayName("archiveBasket: Should create history, snapshot items, and clear basket")
    void archiveBasket_Success() {
        String userId = "user-1";
        String basketId = "basket-1";
        BigDecimal total = new BigDecimal("100.50");
        String basketName = "My Weekly Shopping";

        ShoppingBasketEntity basketEntity = new ShoppingBasketEntity();
        basketEntity.setId(basketId);
        basketEntity.setName(basketName);

        when(basketMapper.findById(basketId)).thenReturn(basketEntity);

        historyService.archiveBasket(userId, basketId, total);

        ArgumentCaptor<HistoryEntity> historyCaptor = ArgumentCaptor.forClass(HistoryEntity.class);
        verify(shoppingHistoryMapper).insertHistoryHeader(historyCaptor.capture());

        HistoryEntity capturedHistory = historyCaptor.getValue();

        assertNotNull(capturedHistory.getId(), "History ID should be generated");
        assertEquals(userId, capturedHistory.getUserId());
        assertEquals(basketId, capturedHistory.getBasketId());
        assertEquals(basketName, capturedHistory.getBasketName());
        assertEquals(100.50, capturedHistory.getTotalSpent());
        assertEquals("BGN", capturedHistory.getCurrency());
        assertNotNull(capturedHistory.getClosedAt()); // Проверяваме, че датата е сложена

        verify(shoppingHistoryMapper).snapshotBasketToHistory(capturedHistory.getId(), basketId);

        verify(basketMapper).clearBasket(basketId);
    }

    @Test
    @DisplayName("archiveBasket: Should throw exception if basket not found")
    void archiveBasket_BasketNotFound() {
        String basketId = "missing-basket";
        when(basketMapper.findById(basketId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                historyService.archiveBasket("user-1", basketId, BigDecimal.TEN)
        );

        assertEquals("Basket not found", ex.getMessage());

        verify(shoppingHistoryMapper, never()).insertHistoryHeader(any());
        verify(shoppingHistoryMapper, never()).snapshotBasketToHistory(anyString(), anyString());
        verify(basketMapper, never()).clearBasket(anyString());
    }

    @Test
    @DisplayName("getUserHistory: Should return list from mapper")
    void getUserHistory() {
        String userId = "user-1";
        List<HistoryEntity> expectedList = Arrays.asList(new HistoryEntity(), new HistoryEntity());
        when(shoppingHistoryMapper.getFullHistoryByUserId(userId)).thenReturn(expectedList);

        List<HistoryEntity> result = historyService.getUserHistory(userId);

        assertEquals(2, result.size());
        verify(shoppingHistoryMapper).getFullHistoryByUserId(userId);
    }

    @Test
    @DisplayName("getLastOrders: Should limit the results")
    void getLastOrders_LimitWorks() {
        String userId = "user-1";
        int limit = 3;

        List<HistoryEntity> fullHistory = IntStream.range(0, 10)
                .mapToObj(i -> HistoryEntity.builder().id("hist-" + i).build())
                .toList();

        when(shoppingHistoryMapper.getFullHistoryByUserId(userId)).thenReturn(fullHistory);

        List<HistoryEntity> result = historyService.getLastOrders(userId, limit);

        assertEquals(3, result.size());
        assertEquals("hist-0", result.get(0).getId()); // Stream.limit запазва реда
        assertEquals("hist-2", result.get(2).getId());
    }

    @Test
    @DisplayName("getLastOrders: Should return all if limit is larger than list size")
    void getLastOrders_ListSmallerThanLimit() {
        String userId = "user-1";
        int limit = 10;
        List<HistoryEntity> smallHistory = Collections.singletonList(new HistoryEntity());

        when(shoppingHistoryMapper.getFullHistoryByUserId(userId)).thenReturn(smallHistory);

        List<HistoryEntity> result = historyService.getLastOrders(userId, limit);

        assertEquals(1, result.size());
    }
}