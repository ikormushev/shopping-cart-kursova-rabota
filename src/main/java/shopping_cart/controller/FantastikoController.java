package shopping_cart.controller;

import shopping_cart.Dto.FantastikoDto;
import shopping_cart.service.FantastikoService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/brochures")
public class FantastikoController {
    private final FantastikoService service;

    public FantastikoController(FantastikoService service) {
        this.service = service;
    }

    @GetMapping("/download")
    public FantastikoDto download() throws Exception {
        System.out.println(">>> CONTROLLER ENTERED /api/brochures/download");

        return service.downloadBrochure();
    }

}
