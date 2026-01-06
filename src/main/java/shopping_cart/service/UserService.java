package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.UserEntity;
import shopping_cart.mapper.UserMapper;
import shopping_cart.model.domain.User;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;
  private final TextEncryptor textEncryptor;

  public void saveUser(User user) {
    var userEntity =
        UserEntity.builder()
            .email(user.getEmail())
            .username(user.getUsername())
            .passwordHash(textEncryptor.encrypt(user.getRawPassword()))
            .location(user.getLocation())
            .build();

    userMapper.insert(userEntity);
  }

  public User findById(UUID id) {
    var userEntity = userMapper.getById(id);
    if (userEntity == null) {
      throw new RuntimeException("No such user");
    }
    return User.builder()
        .email(userEntity.getEmail())
        .location(userEntity.getLocation())
        .id(UUID.fromString(userEntity.getId()))
        .rawPassword(textEncryptor.decrypt(userEntity.getPasswordHash()))
        .build();
  }
}
