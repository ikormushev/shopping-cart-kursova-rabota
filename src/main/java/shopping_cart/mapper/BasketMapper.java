package shopping_cart.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import shopping_cart.entity.BasketItemEntity;
import shopping_cart.entity.BasketMemberEntity;
import shopping_cart.entity.ShoppingBasketEntity;

@Mapper
public interface BasketMapper {

    @Insert("""
        INSERT INTO shopping_baskets (id, name, share_code, owner_id, created_at)
        VALUES (#{id}, #{name}, #{shareCode}, #{ownerId}, #{createdAt})
    """)
    void createBasket(ShoppingBasketEntity basket);

    @Select("SELECT * FROM shopping_baskets WHERE share_code = #{code}")
    @Results(id = "BasketResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "shareCode", column = "share_code"),
            @Result(property = "ownerId", column = "owner_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    ShoppingBasketEntity findByShareCode(String code);

    @Select("SELECT * FROM shopping_baskets WHERE id = #{id}")
    @ResultMap("BasketResultMap")
    ShoppingBasketEntity findById(String id);

    @Insert("""
        INSERT INTO basket_members (basket_id, user_id, role)
        VALUES (#{basketId}, #{userId}, #{role})
    """)
    void addMember(BasketMemberEntity member);

    @Select("SELECT * FROM basket_members WHERE basket_id = #{basketId}")
    List<BasketMemberEntity> findMembers(String basketId);

    @Select("""
    SELECT 
        sb.id, 
        sb.name, 
        sb.share_code, 
        sb.owner_id, 
        sb.created_at,
        bm.user_id as member_id -- вземаме го от join-а
    FROM shopping_baskets sb
    JOIN basket_members bm ON sb.id = bm.basket_id
    WHERE bm.user_id = #{userId}
""")
    @Results(id = "BasketWithMemberMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "shareCode", column = "share_code"),
            @Result(property = "ownerId", column = "owner_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "userId", column = "member_id")
    })
    List<ShoppingBasketEntity> findAllByUserId(String userId);



    @Insert("""
    INSERT INTO basket_items (id, basket_id, product_id, quantity, added_by, added_at)
    VALUES (#{id}, #{basketId}, #{productId}, #{quantity}, #{addedBy}, 
            #{addedAt})
""")
    void addItem(BasketItemEntity item);

    @Select("""
        SELECT 
            bi.id, 
            bi.basket_id, 
            bi.product_id, 
            bi.quantity, 
            bi.added_by, 
            bi.added_at,
            p.raw_name, 
            p.price, 
            s.name as store_name
        FROM basket_items bi
        JOIN products p ON bi.product_id = p.id
        JOIN stores s ON p.store_id = s.id
        WHERE bi.basket_id = #{basketId}
    """)
    @Results(id = "BasketItemResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "basketId", column = "basket_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "addedBy", column = "added_by"),
            @Result(property = "addedAt", column = "added_at"),
            @Result(property = "rawName", column = "raw_name"),
            @Result(property = "price", column = "price"),
            @Result(property = "storeName", column = "store_name")
    })
    List<BasketItemEntity> findItemsByBasketId(String basketId);

    @Select("SELECT * FROM basket_items WHERE id = #{id} AND basket_id = #{basketId}")
    @Results(id = "BasketItemBaseMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "basketId", column = "basket_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "addedBy", column = "added_by"),
            @Result(property = "addedAt", column = "added_at")
    })
    BasketItemEntity findItemById(@Param("basketId") String basketId ,@Param("id") String id);

    @Update("UPDATE basket_items SET quantity = #{quantity} WHERE id = #{id}")
    void updateQuantity(@Param("id") String id, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM basket_items WHERE basket_id = #{basketId} AND product_id = #{productId}")
    void removeItem(@Param("basketId") String basketId, @Param("productId") String productId);

    @Delete("DELETE FROM basket_items WHERE basket_id = #{basketId}")
    void clearBasket(String basketId);

    @Select("SELECT role FROM basket_members WHERE basket_id = #{basketId} AND user_id = #{userId}")
    String getUserRole(@Param("basketId") String basketId, @Param("userId") String userId);

    @Select("SELECT id FROM shopping_baskets WHERE share_code = #{shareCode}")
    String findBasketIdByShareCode(@Param("shareCode") String shareCode);
}
