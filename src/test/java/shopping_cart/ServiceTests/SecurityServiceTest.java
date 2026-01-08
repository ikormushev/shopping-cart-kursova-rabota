package shopping_cart.ServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import shopping_cart.service.SecurityService;

class SecurityServiceTest {

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
    }

    @Test
    @DisplayName("generateSecureToken: Should return a valid UUID string")
    void generateSecureToken() {
        String token = securityService.generateSecureToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertDoesNotThrow(() -> UUID.fromString(token));
    }

    @Test
    @DisplayName("generateSecureToken: Should generate unique tokens")
    void generateSecureToken_Unique() {
        String token1 = securityService.generateSecureToken();
        String token2 = securityService.generateSecureToken();

        assertNotEquals(token1, token2, "Tokens should be unique");
    }

    @Test
    @DisplayName("validatePasswords: Should pass when passwords match")
    void validatePasswords_Success() {
        assertDoesNotThrow(() -> securityService.validatePasswords("password123", "password123"));
    }

    @Test
    @DisplayName("validatePasswords: Should throw exception when passwords do not match")
    void validatePasswords_Mismatch() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            securityService.validatePasswords("password123", "differentPass");
        });

        assertEquals("Passwords do not match!", exception.getMessage());
    }

    @Test
    @DisplayName("validatePasswords: Should throw exception when first password is null")
    void validatePasswords_NullPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            securityService.validatePasswords(null, "somePass");
        });

        assertEquals("Passwords do not match!", exception.getMessage());
    }

    @Test
    @DisplayName("validatePasswords: Should throw exception when confirm password is null")
    void validatePasswords_NullConfirmPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            securityService.validatePasswords("somePass", null);
        });

        assertEquals("Passwords do not match!", exception.getMessage());
    }

    @Test
    @DisplayName("validatePasswords: Should throw exception when both are null")
    void validatePasswords_BothNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            securityService.validatePasswords(null, null);
        });
    }
}