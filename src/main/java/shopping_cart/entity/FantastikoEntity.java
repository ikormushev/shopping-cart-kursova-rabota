package shopping_cart.entity;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
public class FantastikoEntity {
  private Long id;
  private String filename;
  private LocalDate validFrom;
  private LocalDate validTo;
}
