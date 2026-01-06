package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.config.UUIDTypeHandler;
import shopping_cart.entity.ShoppingBasketEntity;

import java.util.List;
import java.util.UUID;

public interface ShoppingBasketMapper {
  @Select("SELECT * FROM shopping_baskets WHERE id = #{id, typeHandler=UUIDTypeHandler}")
  @Results({
    @Result(property = "id", column = "id", typeHandler = UUIDTypeHandler.class),
    @Result(property = "ownerId", column = "owner_id", typeHandler = UUIDTypeHandler.class),
    @Result(property = "isShared", column = "is_shared"),
    @Result(property = "shareCode", column = "share_code")
  })
  ShoppingBasketEntity getById(UUID id);

  @Insert(
      """
        INSERT INTO shopping_baskets (id, owner_id, name, is_shared, share_code, created_at)
        VALUES (#{id, typeHandler=UUIDTypeHandler},
                #{ownerId, typeHandler=UUIDTypeHandler},
                #{name}, #{isShared}, #{shareCode}, NOW())
    """)
  void insert(ShoppingBasketEntity basket);

  @Update(
      "UPDATE shopping_baskets SET name = #{name} WHERE id = #{id, typeHandler=UUIDTypeHandler}")
  void updateName(@Param("id") UUID id, @Param("name") String name);
}
