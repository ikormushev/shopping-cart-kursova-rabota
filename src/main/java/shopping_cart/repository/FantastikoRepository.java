package shopping_cart.repository;

import org.apache.ibatis.annotations.*;
import shopping_cart.entity.FantastikoEntity;

import java.util.List;

@Mapper
public interface FantastikoRepository {

  @Select("SELECT * FROM fantastiko_brochures")
  List<FantastikoEntity> getAll();

  @Insert(
      """
        INSERT INTO fantastiko_brochures (filename, valid_from, valid_to)
        VALUES (#{filename}, #{validFrom}, #{validTo})
    """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(FantastikoEntity brochure);

  @Delete("DELETE FROM fantastiko_brochures WHERE valid_to < CURRENT_DATE")
  void deleteExpired();
}
