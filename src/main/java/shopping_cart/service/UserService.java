package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.UserEntity;
import shopping_cart.mapper.UserMapper;
import shopping_cart.model.domain.User;
import shopping_cart.model.user.request.CreateUserRequest;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;
import shopping_cart.model.user.response.UpdatePasswordResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;
  private final TextEncryptor textEncryptor;
  private final SecurityService securityService;

  public RegisterUserAttemptResponse saveUser(CreateUserRequest user) {
    int errorCode = 1000;
    String message = "OK";
    String uniqueCode = "";
    if (!isUserRegistered(user)) {
      uniqueCode = securityService.generateSecureToken();
      var userEntity =
          UserEntity.builder()
              .id(UUID.randomUUID().toString())
              .email(user.getEmail())
              .username(user.getUsername())
              .passwordHash(textEncryptor.encrypt(user.getPassword()))
              .location(user.getLocation())
              .uniqueCode(uniqueCode)
              .build();

      userMapper.insert(userEntity);
    } else {
      errorCode = 5001;
      message = "Email already used";
    }

    return RegisterUserAttemptResponse.builder()
        .errorCode(errorCode)
        .message(message)
        .uniqueCode(uniqueCode)
        .build();
  }

  public User findById(String id) {
    var userEntity = userMapper.getById(id);
    if (userEntity == null) {
      throw new RuntimeException("No such user");
    }
    return User.builder()
        .email(userEntity.getEmail())
        .uniqueCode(userEntity.getUniqueCode())
        .id(userEntity.getId())
        .rawPassword(textEncryptor.decrypt(userEntity.getPasswordHash()))
        .build();
  }

  public List<User> getAll(String id) {
    var userEntities = userMapper.getAll();
    if (userEntities.isEmpty()) {
      return Collections.emptyList();
    }
    List<User> users = new LinkedList<>();
    for (var userEntity : userEntities) {
      users.add(
          User.builder()
              .email(userEntity.getEmail())
              .id(userEntity.getId())
              .uniqueCode(userEntity.getUniqueCode())
              .rawPassword(textEncryptor.decrypt(userEntity.getPasswordHash()))
              .build());
    }
    return users;
  }

  private boolean isUserRegistered(CreateUserRequest user) {
    var userResult = userMapper.getByEmail(user.getEmail());
    return userResult != null
        && user.getPassword().equalsIgnoreCase(user.getPasswordConfirmation());
  }

  public User getByEmail(String email) {
    var entity = userMapper.getByEmail(email);
    if (entity == null) return null;

    return User.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .uniqueCode(entity.getUniqueCode())
        .rawPassword(textEncryptor.decrypt(entity.getPasswordHash()))
        .build();
  }

  public UpdatePasswordResponse updatePassword(
      String token, String newPassword, String confirmPassword) {
    if (!newPassword.equals(confirmPassword)) {
      throw new RuntimeException("Passwords do not match!");
    }

    if (!newPassword.equalsIgnoreCase(confirmPassword)) {
      throw new RuntimeException("New passwords doesn't match");
    }

    userMapper.updatePasswordByToken(token, textEncryptor.encrypt(newPassword));
    return UpdatePasswordResponse.builder()
        .message("Password Updated successfully")
        .errorCode(1000)
        .build();
  }
}
