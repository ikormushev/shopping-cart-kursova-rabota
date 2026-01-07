package shopping_cart.model.user.response;

import lombok.*;
import lombok.experimental.SuperBuilder;
import shopping_cart.model.response.BaseResponse;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class RegisterUserAttemptResponse extends BaseResponse {
  private String uniqueCode;
}
