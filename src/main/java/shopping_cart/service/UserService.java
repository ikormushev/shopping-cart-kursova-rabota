package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.UserEntity;
import shopping_cart.mapper.UserMapper;
import shopping_cart.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserMapper userMapper;

  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    var entities = userMapper.getAllUsers();
    for (var entity : entities) {
      users.add(new User(entity.getName(), entity.getAge()));
    }
    return users;
  }

  public UserEntity addUser(User user) {
    UserEntity entity = new UserEntity();
    entity.setAge(user.getAge());
    entity.setName(user.getName());
    userMapper.insertUser(entity);

    return userMapper.getAllUsers().stream()
        .filter(entity1 -> entity.getId() == (entity1.getId()))
        .findFirst()
        .orElse(null);
  }
}
