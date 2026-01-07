package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.StoreEntity;

import java.util.List;
import java.util.UUID;

@Mapper
public interface StoreMapper {
    @Select("SELECT * FROM stores")
    List<StoreEntity> findAll();

    @Select("SELECT * FROM stores WHERE id = #{id}")
    StoreEntity findById(String id);

    @Select("SELECT id FROM stores WHERE name = #{name}")
    String findByName(String name);

    @Insert("INSERT INTO stores (id, name, website_url) VALUES (#{id}, #{name}, #{websiteUrl})")
    void insert(StoreEntity store);

}

