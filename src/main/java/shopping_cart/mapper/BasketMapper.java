package shopping_cart.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.ShoppingBasketEntity;
import shopping_cart.entity.StorePriceCalculation;

public interface BasketMapper {

  @Select("SELECT * FROM shopping_baskets WHERE id = #{id}")
  @Results({
    @Result(property = "id", column = "id"),
    @Result(property = "ownerId", column = "owner_id"),
    @Result(property = "isShared", column = "is_shared"),
    @Result(property = "shareCode", column = "share_code")
  })
  ShoppingBasketEntity getById(String id);

  @Insert(
      """
        INSERT INTO shopping_baskets (id, owner_id, name, is_shared, share_code, created_at)
        VALUES (#{id}, #{ownerId}, #{name}, #{isShared}, #{shareCode}, NOW())
    """)
  void insert(ShoppingBasketEntity basket);

  @Update("UPDATE shopping_baskets SET name = #{name} WHERE id = #{id}")
  void updateName(@Param("id") String id, @Param("name") String name);

  @Select(
      """
      SELECT
          s.id AS storeId,
          r.name AS retailerName,
          SUM(p.price * bi.quantity) AS totalCost
      FROM basket_items bi
      JOIN prices p ON bi.product_id = p.product_id
      JOIN stores s ON p.store_id = s.id
      JOIN retailer r ON s.retailer_id = r.id
      WHERE bi.basket_id = #{basketId}
      GROUP BY s.id, r.name
      ORDER BY totalCost ASC
      LIMIT 1
    """)
  StorePriceCalculation getCheapestStore(String basketId);

  @Delete("DELETE FROM basket_items WHERE basket_id = #{basketId}")
  void clearBasketItems(String basketId);
  
  @Insert(
      """
        INSERT INTO basket_items (id, basket_id, product_id, quantity, added_by, added_at)
        SELECT #{id}, #{basketId}, p.product_id, #{quantity}, #{userId}, NOW()
        FROM prices p WHERE p.id = #{priceId}
    """)
  void addItemByPriceId(
      @Param("id") String id,
      @Param("basketId") String basketId,
      @Param("priceId") String priceId,
      @Param("quantity") int quantity,
      @Param("userId") String userId);

  @Delete("DELETE FROM basket_items WHERE basket_id = #{basketId} AND id = #{itemId}")
  void removeItem(@Param("basketId") String basketId, @Param("itemId") String itemId);

  @Select("SELECT * FROM basket_items WHERE basket_id = #{basketId}")
  List<BasketItemEntity> getItemsByBasketId(String basketId);
}
