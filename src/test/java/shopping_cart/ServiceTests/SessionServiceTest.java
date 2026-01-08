package shopping_cart.ServiceTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shopping_cart.entity.SessionEntity;
import shopping_cart.mapper.SessionMapper;
import shopping_cart.model.session.Session;
import shopping_cart.service.SessionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;


    @Test
    @DisplayName("createSession: Should convert model to entity and insert")
    void createSession() {
        Session sessionMock = mock(Session.class);
        UUID sessionId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.getCartId()).thenReturn(cartId);
        when(sessionMock.getStatus()).thenReturn("ACTIVE");

        sessionService.createSession(sessionMock);

        ArgumentCaptor<SessionEntity> entityCaptor = ArgumentCaptor.forClass(SessionEntity.class);
        verify(sessionMapper).insert(entityCaptor.capture());

        SessionEntity capturedEntity = entityCaptor.getValue();

        assertEquals(sessionId.toString(), capturedEntity.getSessionId());
        assertEquals(cartId.toString(), capturedEntity.getCartId());
        assertEquals("ACTIVE", capturedEntity.getStatus());

        assertNotNull(capturedEntity.getCreatedAt());
        assertNotNull(capturedEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("getSession: Should return entity from mapper")
    void getSession() {
        String sessionId = "sess-123";
        SessionEntity expectedEntity = new SessionEntity();
        expectedEntity.setSessionId(sessionId);
        expectedEntity.setStatus("ACTIVE");

        when(sessionMapper.getById(sessionId)).thenReturn(expectedEntity);

        SessionEntity result = sessionService.getSession(sessionId);

        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        verify(sessionMapper).getById(sessionId);
    }

    @Test
    @DisplayName("invalidateSession: Should update status to EXPIRED")
    void invalidateSession() {
        String sessionId = "sess-123";

        sessionService.invalidateSession(sessionId);

        verify(sessionMapper).updateStatus(sessionId, "EXPIRED");
    }

    @Test
    @DisplayName("syncSessionToDb: Should update status from model")
    void syncSessionToDb() {
        Session sessionMock = mock(Session.class);
        UUID sessionId = UUID.randomUUID();

        when(sessionMock.getSessionId()).thenReturn(sessionId);
        when(sessionMock.getStatus()).thenReturn("PENDING");

        sessionService.syncSessionToDb(sessionMock);

        verify(sessionMapper).updateStatus(sessionId.toString(), "PENDING");
    }

    @Test
    @DisplayName("removeSession: Should delete by id")
    void removeSession() {
        String sessionId = "sess-to-delete";

        sessionService.removeSession(sessionId);

        verify(sessionMapper).delete(sessionId);
    }
}