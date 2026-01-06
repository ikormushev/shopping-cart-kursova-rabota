package shopping_cart.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.entity.HistoryItemEntity;

@Mapper
public interface ShoppingHistoryMapper {
  // Saves the main history record (The summary)
  @Insert(
      """
        INSERT INTO shopping_history (id, user_id, basket_id, basket_name, total_spent, currency, closed_at)
        VALUES (#{id}, #{userId}, #{basketId}, #{basketName}, #{totalSpent}, #{currency}, NOW())
    """)
  void insertHistoryHeader(HistoryEntity history);

  // Moves items from the active basket to history items (The snapshot)
  @Insert(
      """
        INSERT INTO shopping_history_items (id, history_id, product_name, quantity, price_at_purchase)
        SELECT LOWER(REPLACE(CAST(gen_random_uuid() AS TEXT), '-', '')),
               #{historyId},
               p.username, -- or p.name depending on your product table column
               bi.quantity,
               pr.price
        FROM basket_items bi
        JOIN products p ON bi.product_id = p.id
        JOIN prices pr ON p.id = pr.product_id
        WHERE bi.basket_id = #{basketId}
          AND pr.store_id = #{chosenStoreId}
    """)
  void archiveItemsFromActiveBasket(
      @Param("historyId") String historyId,
      @Param("basketId") String basketId,
      @Param("chosenStoreId") String chosenStoreId);

  // Retrieves a full history for a user
  @Select("SELECT * FROM shopping_history WHERE user_id = #{userId} ORDER BY closed_at DESC")
  @Results(
      id = "HistoryResultMap",
      value = {
        @Result(property = "id", column = "id"),
        @Result(property = "items", column = "id", many = @Many(select = "getItemsByHistoryId"))
      })
  List<HistoryEntity> getFullHistoryByUserId(String userId);

  @Select("SELECT * FROM shopping_history_items WHERE history_id = #{historyId}")
  List<HistoryItemEntity> getItemsByHistoryId(String historyId);
}
