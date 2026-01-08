package shopping_cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shopping_cart.entity.SessionEntity;
import shopping_cart.mapper.SessionMapper;
import shopping_cart.model.session.Session;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {
  private final SessionMapper sessionMapper;

  public void createSession(Session session) {
    SessionEntity sessionEntity =
        new SessionEntity(
            session.getSessionId().toString(),
            session.getStatus(),
            session.getCartId() == null ? null : session.getCartId().toString(),
            session.getUserId(),
            LocalDateTime.now(),
            LocalDateTime.now());
    sessionMapper.insert(sessionEntity);
  }

  public SessionEntity getSession(String sessionId) {
    return sessionMapper.getById(sessionId);
  }

  public void invalidateSession(String sessionId) {
    sessionMapper.updateStatus(sessionId, "EXPIRED");
  }

  public void syncSessionToDb(Session session) {
    sessionMapper.updateStatus(session.getSessionId().toString(), session.getStatus());
  }

  public void removeSession(String sessionId) {
    sessionMapper.delete(sessionId);
  }
}
