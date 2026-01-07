package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.dto.ProductResponseDTO;
import shopping_cart.entity.PriceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Mapper
public interface PriceMapper {

  @Select("SELECT * FROM prices")
  List<PriceEntity> getAll();

  @Insert(
      """
     INSERT INTO prices (id, product_id, store_id, price, currency, created_at)
     VALUES (#{id}, #{productId}, #{storeId}, #{price}, #{currency}, #{createdAt})
     """)
  void insert(PriceEntity price);

  @Delete("DELETE FROM prices WHERE store_id = #{storeId}")
  void deletePricesByStoreId(@Param("storeId") String storeId);

  @Select(
      """
        SELECT
            p.*,
            s.name as store_name
        FROM prices p
        JOIN stores s ON p.store_id = s.id
        WHERE p.id = #{id}
    """)
  @Select(
      """
        SELECT
            p.*,
            s.name AS store_name
        FROM prices p
        JOIN stores s ON p.store_id = s.id
        WHERE p.id = #{id}
    """)
  @Results(
      id = "PriceWithStoreMap",
      value = {
        @Result(property = "id", column = "id"),
        @Result(property = "productId", column = "product_id"),
        @Result(property = "storeId", column = "store_id"),
        @Result(property = "price", column = "price"),
        @Result(property = "currency", column = "currency"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(
            property = "storeName",
            column = "store_name")
      })
  PriceEntity findById(@Param("id") String id);

    @Select("""
        SELECT 
            p.*, 
            s.name AS store_name 
        FROM prices p
        JOIN stores s ON p.store_id = s.id
        WHERE p.product_id = #{productId}
    """)
    @Results(id = "PriceWithStoreMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "storeId", column = "store_id"),
            @Result(property = "price", column = "price"),
            @Result(property = "currency", column = "currency"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "storeName", column = "store_name")
    })
    List<PriceEntity> findByProductId(@Param("productId") String productId);


    @Results(id = "PriceWithStoreMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "storeId", column = "store_id"),
            @Result(property = "price", column = "price"),
            @Result(property = "currency", column = "currency"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "storeName", column = "store_name")
    })
    PriceEntity findBySingleProductId(@Param("productId") String productId);

  @Select(
      """
        SELECT
            p.id as price_id,
            p.price,
            p.currency,
            pr.name as product_name,
            pr.image_url,
            s.name as store_name
        FROM prices p
        JOIN products pr ON p.product_id = pr.id
        JOIN stores s ON p.store_id = s.id
        WHERE p.id = #{priceId}
    """)
  ProductResponseDTO findPriceWithDetails(@Param("priceId") String priceId);
}
