package shopping_cart.model.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PingResponse extends BaseResponse {
  private boolean isSharedCartUpdated;
}
