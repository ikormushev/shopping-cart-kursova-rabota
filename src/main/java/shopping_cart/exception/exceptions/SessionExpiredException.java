package shopping_cart.exception.exceptions;

import lombok.Getter;

@Getter
public class SessionExpiredException extends RuntimeException {
  private static final int ERROR_CODE = 3005;
  private static final String EXCEPTION_MESSAGE = "Session expired, please log again";
  private final int errorCode;

  public SessionExpiredException() {
    super(EXCEPTION_MESSAGE);
    this.errorCode = ERROR_CODE;
  }
}
