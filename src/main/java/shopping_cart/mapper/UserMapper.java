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
  UserEntity getById(String id);

  @Results(
      id = "userMap",
      value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "location", column = "location"),
        @Result(property = "uniqueCode", column = "unique_code"),
        @Result(property = "createdAt", column = "created_at")
      })
  @Select(
      "SELECT id, username, email, password_hash, location, unique_code, created_at FROM app_user WHERE email = #{email}")
  UserEntity getByEmail(String email);

  @Insert(
"""
    INSERT INTO app_user (id, username, email, password_hash, unique_code, location, created_at)
    VALUES (#{id}, #{username}, #{email}, #{passwordHash}, #{uniqueCode}, #{location}, NOW())
""")
  void insert(UserEntity user);

  @Update(
      """
        UPDATE app_user
        SET password_hash = #{newPassword}
        WHERE unique_code = #{token}
    """)
  void updatePasswordByToken(
      @Param("token") String token, @Param("newPassword") String newPassword);

  @Update("UPDATE app_user SET password_hash = #{newPassword} WHERE email = #{email}")
  void updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);

  @Update("UPDATE app_user SET username = #{newUsername} WHERE email = #{email}")
  void updateUsernameByEmail(@Param("email") String email, @Param("newUsername") String newUsername);
}
