package shopping_cart.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shopping_cart.mapper.SessionMapper;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionManager {
  private final SessionCacheRepository sessionCache;
  private final SessionMapper sessionMapper;

  public Session getSession(String sessionId) {
    UUID sessionUuid = UUID.fromString(sessionId);
    Session session = sessionCache.get(sessionUuid);

    if (session == null) {
      throw new RuntimeException("Session invalid or totally expired");
    }

    if (session.getCartId() == null) {
      log.info("Cache miss for cartId in session {}. Attempting DB recovery...", sessionId);
      String recoveredCartId = sessionMapper.findLastActiveCartId(session.getUserId());

      if (recoveredCartId != null) {
        session.setCartId(UUID.fromString(recoveredCartId));
        sessionCache.update(sessionUuid, session);
        log.info("Recovered cartId {} from DB for session {}", recoveredCartId, sessionId);
      }
    }

    return session;
  }

  public void update(UUID sessionId, Session session) {
    sessionCache.update(sessionId, session);
    sessionMapper.updateSession(sessionId.toString(), session.getCartId().toString(), "ACTIVE");
  }

  public void removeCartIdAfterCheckout(String sessionId) {
    sessionCache.evictShoppingCartFromSession(sessionId);
  }

  @Transactional
  public void switchActiveCart(String sessionId, String basketId) {
    UUID sessionUuid = UUID.fromString(sessionId);
    Session session = getSession(sessionId);

    session.setCartId(UUID.fromString(basketId));
    sessionCache.update(sessionUuid, session);

    sessionMapper.updateSession(sessionId, basketId, session.getStatus());
  }
}
