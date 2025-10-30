package shopping_cart.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import shopping_cart.entity.UserEntity;

import java.util.List;

@Mapper
public interface UserMapper {
  @Select("SELECT id, name, age FROM app_user")
  List<UserEntity> getAllUsers();

  @Insert("INSERT INTO app_user (id, name, age) VALUES (#{id}, #{name}, #{age})")
  void insertUser(UserEntity user);
}
