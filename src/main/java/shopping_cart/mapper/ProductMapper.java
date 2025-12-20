package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.ProductEntity;
import shopping_cart.config.UUIDTypeHandler;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ProductMapper {
    @Insert("""
        INSERT INTO products (id, name, category, description, sku, created_at)
        VALUES (
            #{id, typeHandler=shopping_cart.config.UUIDTypeHandler}, 
            #{name}, 
            #{category}, 
            #{description}, 
            #{sku}, 
            #{createdAt}
        )
    """)
    void insert(ProductEntity product);
    @Select("SELECT * FROM products WHERE sku = #{sku} LIMIT 1")
    @Results(value = {
            @Result(property = "id", column = "id", typeHandler = UUIDTypeHandler.class),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "name", column = "name"),
            @Result(property = "sku", column = "sku"),
            @Result(property = "category", column = "category"),
            @Result(property = "description", column = "description")
    })
    ProductEntity findBySku(@Param("sku") String sku);
}
