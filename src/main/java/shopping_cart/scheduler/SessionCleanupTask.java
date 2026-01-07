package shopping_cart.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import shopping_cart.model.session.Session;
import shopping_cart.repository.cache.SessionCacheRepository;
import shopping_cart.service.SessionService;

import java.time.LocalDateTime;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SessionCleanupTask {

  private final SessionCacheRepository cacheRepository;
  private final SessionService sessionService;

  @Scheduled(fixedRate = 60000)
  public void cleanupExpiredSessions() {
    LocalDateTime now = LocalDateTime.now();
    cacheRepository
        .getAllKeepAlives()
        .forEach(
            (sessionId, expiryTime) -> {
              if (expiryTime.isBefore(now)) {

                Session cachedSession = cacheRepository.get(sessionId);

                if (cachedSession != null) {
                  log.info("Syncing and expiring session: {}", sessionId);

                  cachedSession.setStatus("EXPIRED");

                  sessionService.syncSessionToDb(cachedSession);

                  cacheRepository.evictSession(sessionId);
                }
              }
            });
  }
}
