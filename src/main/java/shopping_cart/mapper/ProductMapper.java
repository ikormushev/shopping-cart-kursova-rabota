package shopping_cart.mapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.*;
import shopping_cart.entity.ProductEntity;
import shopping_cart.entity.ProductGroupEntity;

@Mapper
public interface ProductMapper {

  @Select(
      """
        SELECT
            id,
            canonical_name AS canonicalName,
            category,
            image_url AS imageUrl
        FROM product_groups
    """)
  List<ProductGroupEntity> findAllGroups();

  @Update(
      """
        UPDATE products
        SET price = #{price}
        WHERE id = #{id}
    """)
  void updatePrice(@Param("id") String id, @Param("price") BigDecimal price);

  @Select(
      """
        SELECT
            id,
            canonical_name AS canonicalName,
            category,
            image_url AS imageUrl
        FROM product_groups
        WHERE id = #{id}
    """)
  ProductGroupEntity findGroupById(String id);

  @Select(
      """
        SELECT p.*, s.name as store_name
        FROM products p
        JOIN stores s ON p.store_id = s.id
        WHERE p.group_id = #{groupId}
    """)
  @Results(
      id = "ProductWithStoreMap",
      value = {
        @Result(property = "id", column = "id"),
        @Result(property = "groupId", column = "group_id"),
        @Result(property = "storeId", column = "store_id"),
        @Result(property = "rawName", column = "raw_name"),
        @Result(property = "sku", column = "sku"),
        @Result(property = "price", column = "price"),
        @Result(property = "currency", column = "currency"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "storeName", column = "store_name")
      })
  List<ProductEntity> findByGroupId(@Param("groupId") String groupId);

  @Insert(
      """
        INSERT INTO products (id, group_id, store_id, raw_name, sku, price, currency, created_at)
        VALUES (#{id}, #{groupId}, #{storeId}, #{rawName}, #{sku}, #{price}, #{currency}, #{createdAt})
    """)
  void insert(ProductEntity product);

  // 1. GLOBAL SEARCH: Find all products regardless of group
  // Useful for the initial "Browse everything" view
  @Select(
      """
        SELECT p.*, s.name as store_name
        FROM products p
        JOIN stores s ON p.store_id = s.id
    """)
  @ResultMap("ProductWithStoreMap")
  List<ProductEntity> findAllProducts();

  // 2. SEARCH BY NAME: Filter the global list
  @Select(
      """
        SELECT p.*, s.name as store_name
        FROM products p
        JOIN stores s ON p.store_id = s.id
        WHERE p.raw_name ILIKE CONCAT('%', #{query}, '%')
    """)
  @ResultMap("ProductWithStoreMap")
  List<ProductEntity> searchProductsByName();

  @Select(
      """
        SELECT p.*, s.name as store_name
        FROM products p
        JOIN stores s ON p.store_id = s.id
        WHERE p.raw_name = #{rawName}
        AND p.store_id = #{storeId}
        LIMIT 1
    """)
  @ResultMap("ProductWithStoreMap")
  ProductEntity findByRawNameAndStore(
      @Param("rawName") String rawName, @Param("storeId") String storeId);

  @Select(
      """
        SELECT p.*, s.name as store_name
        FROM products p
        JOIN stores s ON p.store_id = s.id
        WHERE p.id = #{id}
    """)
  @ResultMap("ProductWithStoreMap")
  ProductEntity findById(@Param("id") String id);
}
