package shopping_cart.ServiceTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import shopping_cart.entity.UserEntity;
import shopping_cart.mapper.UserMapper;
import shopping_cart.model.domain.User;
import shopping_cart.model.user.request.CreateUserRequest;
import shopping_cart.model.user.response.RegisterUserAttemptResponse;
import shopping_cart.model.user.response.UpdatePasswordResponse;
import shopping_cart.service.UserService;
import shopping_cart.service.SecurityService;


import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private TextEncryptor textEncryptor;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("saveUser: Should register successfully when user does not exist")
    void saveUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setPasswordConfirmation("password123");
        request.setLocation("Sofia");

        // Мокваме, че няма такъв имейл в базата
        when(userMapper.getByEmail(request.getEmail())).thenReturn(null);
        when(securityService.generateSecureToken()).thenReturn("secure-token-123");
        when(textEncryptor.encrypt(request.getPassword())).thenReturn("encrypted-password");

        RegisterUserAttemptResponse response = userService.saveUser(request);

        assertEquals(1000, response.getErrorCode());
        assertEquals("OK", response.getMessage());
        assertEquals("secure-token-123", response.getUniqueCode());

        verify(userMapper, times(1)).insert(any(UserEntity.class));
    }

    @Test
    @DisplayName("saveUser: Should return error when user email already exists")
    void saveUser_EmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@example.com");
        request.setPassword("pass");
        request.setPasswordConfirmation("pass");

        UserEntity existingUser = UserEntity.builder().email("existing@example.com").build();

        when(userMapper.getByEmail(request.getEmail())).thenReturn(existingUser);

        RegisterUserAttemptResponse response = userService.saveUser(request);

        assertEquals(5001, response.getErrorCode());
        assertEquals("Email already used", response.getMessage());

        verify(userMapper, never()).insert(any(UserEntity.class));
    }

    @Test
    @DisplayName("findById: Should return User domain object when found")
    void findById_Success() {
        String userId = UUID.randomUUID().toString();
        UserEntity entity = UserEntity.builder()
                .id(userId)
                .email("test@test.com")
                .uniqueCode("code123")
                .passwordHash("hashedPass")
                .build();

        when(userMapper.getById(userId)).thenReturn(entity);
        when(textEncryptor.decrypt("hashedPass")).thenReturn("rawPass");

        User result = userService.findById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("rawPass", result.getRawPassword());
    }

    @Test
    @DisplayName("findById: Should throw RuntimeException when user not found")
    void findById_NotFound() {
        String userId = "non-existent-id";
        when(userMapper.getById(userId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findById(userId);
        });
        assertEquals("No such user", exception.getMessage());
    }

    @Test
    @DisplayName("getAll: Should return list of users")
    void getAll_Success() {
        UserEntity entity = UserEntity.builder()
                .email("a@a.com")
                .passwordHash("hash")
                .build();

        when(userMapper.getAll()).thenReturn(List.of(entity));
        when(textEncryptor.decrypt("hash")).thenReturn("raw");

        List<User> users = userService.getAll("some-id"); // ID-то в метода не се ползва в логиката, но го подаваме

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("raw", users.get(0).getRawPassword());
    }

    @Test
    @DisplayName("getAll: Should return empty list when no users found")
    void getAll_Empty() {
        when(userMapper.getAll()).thenReturn(Collections.emptyList());

        List<User> users = userService.getAll("id");

        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("updatePassword: Should update password when inputs match")
    void updatePassword_Success() {
        String token = "valid-token";
        String newPass = "NewPass123";
        String confirmPass = "NewPass123";

        when(textEncryptor.encrypt(newPass)).thenReturn("encrypted-new-pass");

        UpdatePasswordResponse response = userService.updatePassword(token, newPass, confirmPass);

        assertEquals(1000, response.getErrorCode());
        verify(userMapper).updatePasswordByToken(token, "encrypted-new-pass");
    }

    @Test
    @DisplayName("updatePassword: Should throw exception when passwords do not match")
    void updatePassword_Mismatch() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword("token", "pass1", "pass2");
        });

        assertEquals("Passwords do not match!", ex.getMessage());
        verify(userMapper, never()).updatePasswordByToken(anyString(), anyString());
    }
}