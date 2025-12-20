//package shopping_cart.scheduler;

//import shopping_cart.service.FantastikoService;
//import org.springframework.stereotype.Component;
//import org.springframework.scheduling.annotation.Scheduled;

//@Component
//public class Scheduler {
//    private final FantastikoService fantastikoService;
//    public Scheduler(FantastikoService fantastikoService)
//    {
//        this.fantastikoService = fantastikoService;
//    }
//    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
//    public void dailyRun() throws Exception {
//        fantastikoService.downloadBrochure();
//    }
//}
