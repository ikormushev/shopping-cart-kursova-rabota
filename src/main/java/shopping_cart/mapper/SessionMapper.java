package shopping_cart.mapper;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.SessionEntity;

@Mapper
public interface SessionMapper {

  @Select("SELECT * FROM session WHERE session_id = #{sessionId}")
  SessionEntity getById(String sessionId);

  @Insert(
      """
        INSERT INTO session (session_id, status, cart_id, user_id, created_at, updated_at)
        VALUES (#{sessionId}, #{status}, #{cartId}, #{userId}, #{createdAt}, #{updatedAt})
    """)
  void insert(SessionEntity session);

  @Update(
      """
        UPDATE session
        SET status = #{status}, updated_at = NOW()
        WHERE session_id = #{sessionId}
    """)
  void updateStatus(@Param("sessionId") String sessionId, @Param("status") String status);

  @Delete("DELETE FROM session WHERE session_id = #{sessionId}")
  void delete(String sessionId);

  @Update(
      """
      UPDATE session
      SET status = #{status},
          updated_at = NOW(),
          cart_id = #{cartId}
      WHERE session_id = #{sessionId}
  """)
  void updateSession(
      @Param("sessionId") String sessionId,
      @Param("cartId") String cartId,
      @Param("status") String status);

  @Select(
      """
        SELECT cart_id FROM session
        WHERE user_id = #{userId} AND status = 'ACTIVE'
        ORDER BY updated_at DESC LIMIT 1
    """)
  String findLastActiveCartId(@Param("userId") String userId);
}
