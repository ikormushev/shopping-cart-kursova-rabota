package shopping_cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shopping_cart.facade.UserFacade;
import shopping_cart.model.user.request.CreateUserRequest;
import shopping_cart.model.user.request.LoginRequest;
import shopping_cart.model.user.request.UpdatePasswordRequest;
import shopping_cart.model.user.response.LoginUserResponse;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;
import shopping_cart.model.user.response.UpdatePasswordResponse;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  private final UserFacade userFacade;

  @PutMapping
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
}
