package shopping_cart.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.entity.HistoryItemEntity;

@Mapper
public interface ShoppingHistoryMapper {

    @Insert("""
    INSERT INTO shopping_history (id, user_id, basket_id, basket_name, total_spent, currency, closed_at)
    VALUES (#{id}, #{userId}, #{basketId}, #{basketName}, #{totalSpent}, #{currency}, #{closedAt})
  """)
    void insertHistoryHeader(HistoryEntity history);

    /**
     * SNAPSHOT LOGIC: Copies items from the active basket into history.
     * Note: We use the product's raw_name and price at THIS MOMENT.
     */
    @Insert("""
    INSERT INTO shopping_history_items (id, history_id, product_name, quantity, price_at_purchase)
    SELECT 
        gen_random_uuid(), 
        #{historyId}, 
        p.raw_name, 
        bi.quantity, 
        p.price
    FROM basket_items bi
    JOIN products p ON bi.product_id = p.id
    WHERE bi.basket_id = #{basketId}
  """)
    void snapshotBasketToHistory(@Param("historyId") String historyId, @Param("basketId") String basketId);

    @Select("SELECT * FROM shopping_history WHERE user_id = #{userId} ORDER BY closed_at DESC")
    @Results(id = "HistoryResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "basketId", column = "basket_id"),
            @Result(property = "basketName", column = "basket_name"),
            @Result(property = "totalSpent", column = "total_spent"),
            @Result(property = "currency", column = "currency"),
            @Result(property = "closedAt", column = "closed_at"),
            @Result(property = "items", column = "id", many = @Many(select = "getItemsByHistoryId"))
    })
    List<HistoryEntity> getFullHistoryByUserId(String userId);

    @Select("SELECT id, history_id as historyId, product_name as productName, quantity, price_at_purchase as priceAtPurchase " +
            "FROM shopping_history_items WHERE history_id = #{historyId}")
    List<HistoryItemEntity> getItemsByHistoryId(String historyId);

    @Insert("""
    INSERT INTO shopping_history_items (id, history_id, product_name, quantity, price_at_purchase)
    SELECT 
        gen_random_uuid(), 
        #{historyId}, 
        p.raw_name, 
        bi.quantity, 
        p.price
    FROM basket_items bi
    JOIN products p ON bi.product_id = p.id
    WHERE bi.basket_id = #{basketId} AND bi.checked = true
  """)
    void snapshotCheckedItemsToHistory(@Param("historyId") String historyId, @Param("basketId") String basketId);
}