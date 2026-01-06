package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.UserEntity;
import shopping_cart.mapper.UserMapper;
import shopping_cart.model.domain.User;
import shopping_cart.model.user.request.CreateUserRequest;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;
  private final TextEncryptor textEncryptor;

  public RegisterUserAttemptResponse saveUser(CreateUserRequest user) {
    int errorCode = 1000;
    String message = "OK";
    if (!isUserRegistered(user)) {
      var userEntity =
          UserEntity.builder()
              .email(user.getEmail())
              .username(user.getUsername())
              .passwordHash(textEncryptor.encrypt(user.getPassword()))
              .location(user.getLocation())
              .build();

      userMapper.insert(userEntity);
    } else {
      errorCode = 5001;
      message = "Email already used";
    }

    return RegisterUserAttemptResponse.builder().errorCode(errorCode).message(message).build();
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

  public List<User> getAll(UUID id) {
    var userEntities = userMapper.getAll();
    if (userEntities.isEmpty()) {
      return Collections.emptyList();
    }
    List<User> users = new LinkedList<>();
    for (var userEntity : userEntities) {
      users.add(
          User.builder()
              .email(userEntity.getEmail())
              .location(userEntity.getLocation())
              .id(UUID.fromString(userEntity.getId()))
              .rawPassword(textEncryptor.decrypt(userEntity.getPasswordHash()))
              .build());
    }
    return users;
  }

  private boolean isUserRegistered(CreateUserRequest user) {
    var userResult = userMapper.getByEmail(user.getEmail());
    return userResult == null;
  }

  public User getByEmail(String email) {
    var entity = userMapper.getByEmail(email);
    if (entity == null) return null;

    return User.builder()
        .id(UUID.fromString(entity.getId()))
        .email(entity.getEmail())
        .location(entity.getLocation())
        .rawPassword(textEncryptor.decrypt(entity.getPasswordHash()))
        .build();
  }
}
