package shopping_cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.facade.UserFacade;
import shopping_cart.model.user.request.*;
import shopping_cart.model.user.response.ChangeUsernameResponse;
import shopping_cart.model.user.response.LoginUserResponse;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;
import shopping_cart.model.user.response.UpdatePasswordResponse;
import shopping_cart.model.user.response.UserResponseDto;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserFacade userFacade;

  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Session-Id") String sessionId) {
    return ResponseEntity.ok(userFacade.getCurrentUser(sessionId));
  }

  @PutMapping("/register")
  public ResponseEntity<RegisterUserAttemptResponse> registerUser(
      @Valid @RequestBody CreateUserRequest createUserRequest) {
    return ResponseEntity.ok(userFacade.register(createUserRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginUserResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(userFacade.login(loginRequest));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<UpdatePasswordResponse> resetPassword(
      @RequestBody UpdatePasswordRequest updatePasswordRequest) {

    return ResponseEntity.ok(
        userFacade.updatePassword(
            updatePasswordRequest.getToken(),
            updatePasswordRequest.getNewPassword(),
            updatePasswordRequest.getConfirmPassword()));
  }

  @PostMapping("/change-password")
  public ResponseEntity<UpdatePasswordResponse> changePassword(
      @RequestBody ChangePasswordRequest request) {

    return ResponseEntity.ok(userFacade.changePassword(request));
  }

  @PostMapping("/change-username")
  public ResponseEntity<ChangeUsernameResponse> changeUsername(
      @RequestBody ChangeUsernameRequest request) {

    return ResponseEntity.ok(userFacade.changeUsername(request));
  }
}
