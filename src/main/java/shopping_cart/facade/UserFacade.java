package shopping_cart.facade;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shopping_cart.model.domain.User;
import shopping_cart.model.session.Session;
import shopping_cart.model.user.request.ChangePasswordRequest;
import shopping_cart.model.user.request.ChangeUsernameRequest;
import shopping_cart.model.user.request.CreateUserRequest;
import shopping_cart.model.user.request.LoginRequest;
import shopping_cart.model.user.response.ChangeUsernameResponse;
import shopping_cart.model.user.response.LoginUserResponse;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;
import shopping_cart.model.user.response.UpdatePasswordResponse;
import shopping_cart.model.user.response.UserResponseDto;
import shopping_cart.repository.cache.SessionCacheRepository;
import shopping_cart.service.SessionService;
import shopping_cart.service.UserService;

@Component
@RequiredArgsConstructor
public class UserFacade {
  private final UserService userService;
  private final SessionCacheRepository sessionCache;
  private final SessionService sessionService;

  public RegisterUserAttemptResponse register(CreateUserRequest request) {
    RegisterUserAttemptResponse response = userService.saveUser(request);
    return response;
  }

  public LoginUserResponse login(LoginRequest loginRequest) {
    User user = userService.getByEmail(loginRequest.getEmail());

    if (user == null || !user.getRawPassword().equals(loginRequest.getPassword())) {
      return LoginUserResponse.builder()
          .errorCode(5001)
          .message("Invalid email or password")
          .build();
    }

    Optional<UUID> existingSessionId =
        sessionCache.getAllSessions().entrySet().stream()
            .filter(entry -> entry.getValue().getUserId().equals(user.getId().toString()))
            .map(Map.Entry::getKey)
            .findFirst();

    if (existingSessionId.isPresent()) {
      sessionCache.updateSessionKeepAlive(existingSessionId.get());
      return LoginUserResponse.builder()
          .errorCode(5002)
          .message("User already has an active session")
          .sessionId(existingSessionId.get().toString())
          .build();
    }

    UUID newSessionId = UUID.randomUUID();
    Session newSession = Session.builder().sessionId(newSessionId).userId(user.getId()).build();

    sessionService.createSession(newSession);
    sessionCache.put(newSessionId, newSession);

    return LoginUserResponse.builder()
        .errorCode(1000)
        .message("OK")
        .sessionId(newSessionId.toString())
        .build();
  }

  public UpdatePasswordResponse updatePassword(
      String token, String newPassword, String confirmPassword) {
    return userService.updatePassword(token, newPassword, confirmPassword);
  }

  public UpdatePasswordResponse changePassword(ChangePasswordRequest request) {
    return userService.changePassword(
        request.getEmail(),
        request.getOldPassword(),
        request.getNewPassword(),
        request.getConfirmPassword());
  }

  public ChangeUsernameResponse changeUsername(ChangeUsernameRequest request) {
    return userService.changeUsername(request.getEmail(), request);
  }

  public UserResponseDto getCurrentUser(String sessionId) {
    Session session = sessionCache.get(UUID.fromString(sessionId));
    if (session == null) {
      throw new RuntimeException("Invalid session");
    }

    User user = userService.findById(session.getUserId());
    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        null,
        null
    );
  }
}
