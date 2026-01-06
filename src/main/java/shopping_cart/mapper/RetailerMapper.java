package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.RetailerEntity;

import java.util.List;
import java.util.UUID;

@Mapper
public interface RetailerMapper {
  @Insert(
      """
        INSERT INTO retailers (id, name, website_url, created_at)
        VALUES (#{id, typeHandler=UUIDTypeHandler}, #{name}, #{websiteUrl}, NOW())
    """)
  void insert(RetailerEntity retailer);

  @Select("SELECT * FROM retailers WHERE id = #{id, typeHandler=UUIDTypeHandler}")
  RetailerEntity findById(UUID id);
}
