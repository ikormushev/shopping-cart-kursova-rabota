package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.config.UUIDTypeHandler;
import shopping_cart.entity.HistoryEntity;
import shopping_cart.entity.HistoryItemEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper
public interface ShoppingHistoryMapper {

  @Insert(
      """
        INSERT INTO shopping_history (id, user_id, basket_name, total_spent, currency, closed_at)
        VALUES (#{id, typeHandler=UUIDTypeHandler},
                #{userId, typeHandler=UUIDTypeHandler},
                #{basketName}, #{totalSpent}, #{currency}, NOW())
    """)
  default void insertHistoryHeader(
      @Param("id") UUID id,
      @Param("userId") UUID userId,
      @Param("basketName") String basketName,
      @Param("totalSpent") BigDecimal totalSpent,
      @Param("currency") String currency) {}

  @Insert(
      """
        INSERT INTO shopping_history_items (id, history_id, product_name, quantity, price_at_purchase)
        VALUES (gen_random_uuid(),
                #{historyId, typeHandler=UUIDTypeHandler},
                #{productName}, #{quantity}, #{priceAtPurchase})
    """)
  void insertHistoryItem(
      @Param("historyId") UUID historyId,
      @Param("productName") String productName,
      @Param("quantity") int quantity,
      @Param("priceAtPurchase") BigDecimal priceAtPurchase);

  @Select("SELECT * FROM shopping_history_items WHERE history_id = #{historyId}")
  List<HistoryItemEntity> findItemsByHistoryId(UUID historyId);

  @Select("SELECT * FROM shopping_history WHERE user_id = #{userId} ORDER BY closed_at DESC")
  @Results({
    @Result(property = "id", column = "id", typeHandler = UUIDTypeHandler.class),
    @Result(property = "basketId", column = "basket_id", typeHandler = UUIDTypeHandler.class),
    @Result(property = "items", column = "id", many = @Many(select = "findItemsByHistoryId"))
  })
  List<HistoryEntity> findFullHistoryByUserId(UUID userId);

  @Select("SELECT * FROM shopping_history WHERE basket_id = #{basketId}")
  @ResultMap("FullHistoryMap")
  List<HistoryEntity> findByBasketId(UUID basketId);

  @Select(
"""
    SELECT
        h.*,
        b.is_shared,
        b.share_code
    FROM shopping_history h
    LEFT JOIN shopping_baskets b ON h.basket_id = b.id
    WHERE h.user_id = #{userId}
""")
  List<HistoryEntity> getHistoryWithLiveMetadata(UUID userId);
}
