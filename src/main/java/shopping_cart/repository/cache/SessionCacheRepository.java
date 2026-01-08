package shopping_cart.repository.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import shopping_cart.model.session.Session;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class SessionCacheRepository {
  private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
  private final Map<UUID, LocalDateTime> sessionKeepAlive = new ConcurrentHashMap<>();

  public void put(UUID sessionId, Session session) {
    sessions.put(sessionId, session);
    updateSessionKeepAlive(sessionId);
  }

  public Session get(UUID sessionId) {
    return sessions.get(sessionId);
  }

  public void updateSessionKeepAlive(UUID sessionId) {
    var newExpiryTime = LocalDateTime.now().plusMinutes(15);
    sessionKeepAlive.put(sessionId, newExpiryTime);
  }

  public void update(UUID sessionId, Session session) {
    if (sessions.containsKey(sessionId)) {
      sessions.put(sessionId, session);
      updateSessionKeepAlive(sessionId);
      log.info("Session {} updated in cache with cartId: {}", sessionId, session.getCartId());
    } else {
      log.warn("Attempted to update non-existent session: {}", sessionId);
    }
  }

  public void evictShoppingCartFromSession(String sessionId) {
    var session = sessions.get(UUID.fromString(sessionId));
    session.setCartId(null);
    sessions.put(UUID.fromString(sessionId), session);
  }

  public void evictSession(UUID sessionId) {
    sessions.remove(sessionId);
    sessionKeepAlive.remove(sessionId);
    log.info("Session {} evicted from cache", sessionId);
  }

  public Map<UUID, LocalDateTime> getAllKeepAlives() {
    return Collections.unmodifiableMap(sessionKeepAlive);
  }

  public Map<UUID, Session> getAllSessions() {
    return Collections.unmodifiableMap(sessions);
  }
}
