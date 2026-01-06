package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.WatchlistItemEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper
public interface WatchlistItemMapper {
    @Select("SELECT * FROM watchlist_item WHERE user_id = #{userId}")
    List<WatchlistItemEntity> findByUser(UUID userId);

    @Insert("""
        INSERT INTO watchlist_item (id, user_id, product_id, target_price, created_at)
        VALUES (#{id, typeHandler=shopping_cart.config.UUIDTypeHandler}, 
        #{userId, typeHandler=shopping_cart.config.UUIDTypeHandler}, 
        #{productId, typeHandler=shopping_cart.config.UUIDTypeHandler}, 
        #{targetPrice}, #{createdAt})
    """)
    void insert(WatchlistItemEntity item);
}