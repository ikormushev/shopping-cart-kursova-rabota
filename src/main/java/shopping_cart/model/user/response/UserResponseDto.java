package shopping_cart.model.user.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDto(
        String id,
        String username,
        String email,
        String location,
        OffsetDateTime createdAt
) {}
