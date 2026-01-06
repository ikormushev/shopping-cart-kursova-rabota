package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.UserEntity;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {
  @Select("SELECT * FROM app_user")
  List<UserEntity> getAll();

  @Select("SELECT * FROM app_user WHERE id = #{id}")
  UserEntity getById(UUID id);

  @Select("SELECT * FROM app_user WHERE email = #{email}")
  UserEntity getByEmail(String id);

  @Insert(
      """
        INSERT INTO app_user (id, username, email, password_hash, unique_code, location, created_at)
        VALUES (#{id}, #{username}, #{email}, #{passwordHash}, #{uniqueCode}, #{location}, #{createdAt})
    """)
  void insert(UserEntity user);

  @Update(
      """
        UPDATE app_user
        SET password_hash = #{newPassword}
        WHERE verification_code = #{token}
    """)
  void updatePasswordByToken(
      @Param("token") String token, @Param("newPassword") String newPassword);
}
