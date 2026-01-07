//package shopping_cart.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import shopping_cart.Dto.BillaDto;
//import shopping_cart.service.BillaService;
//
//@RestController
//@RequestMapping("/api/billa")
//public class BillaController {
//
//    private final BillaService billaService;
//
//    public BillaController(BillaService billaService) {
//        this.billaService = billaService;
//    }
//
//    @GetMapping("/sync")
//    public ResponseEntity<String> syncBrochure() {
//        try {
//            BillaDto result = billaService.downloadBrochure();
//            return ResponseEntity.ok(
//                    String.format("Успешно свалена брошура: %s. Валидна от %s до %s",
//                            result.getFileName(), result.getStartDate(), result.getEndDate())
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Грешка при сваляне на брошурата: " + e.getMessage());
//        }
//    }
//}