package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.PriceEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Mapper
public interface PriceMapper {

    @Select("SELECT * FROM prices")
    List<PriceEntity> getAll();

    @Insert("""
     INSERT INTO prices (id, product_id, store_id, price, currency, created_at) -- ТУК ВЕЧЕ СА 6 КОЛОНИ (+currency)
     VALUES (#{id, typeHandler=shopping_cart.config.UUIDTypeHandler},
             #{productId, typeHandler=shopping_cart.config.UUIDTypeHandler},
             #{storeId, typeHandler=shopping_cart.config.UUIDTypeHandler},
             #{price}, 
             #{currency},  
             #{createdAt})
     """)

    void insert(PriceEntity price);
    @Delete("DELETE FROM shopping_cart.prices WHERE store_id = #{storeId}")
    void deletePricesByStoreId(@Param("storeId") UUID storeId);


    @Select("""
     SELECT * FROM prices
     WHERE product_id = #{productId}
       AND store_id = #{storeId}
       AND DATE(created_at) = #{date} 
     """)
    List<PriceEntity> findByProductAndStoreAndDate(
            @Param("productId") UUID productId,
            @Param("storeId") UUID storeId,
            @Param("date") LocalDate date);
}