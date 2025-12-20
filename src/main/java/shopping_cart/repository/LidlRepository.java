//package shopping_cart.repository;

//import org.springframework.data.jpa.repository.JpaRepository;
//import java.time.LocalDate;
//import java.util.List;

//public interface LidlRepository extends JpaRepository<LidlProduct, Long> {
//    List<LidlProduct> findByScrapedAt(LocalDate scrapedAt);
//    void deleteByScrapedAtBefore(LocalDate date); // за почистване на стари данни
//}