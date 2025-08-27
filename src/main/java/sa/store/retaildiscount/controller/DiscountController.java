package sa.store.retaildiscount.controller;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountResponse;
import sa.store.retaildiscount.service.BillService;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final BillService billService;

    @Autowired
    public DiscountController(BillService billService) {
        this.billService = billService;
    }

    private final Logger log = org.slf4j.LoggerFactory.getLogger(DiscountController.class);

    @PostMapping("")
    public ResponseEntity<DiscountResponse> calculateDiscount(@RequestBody BillRequest billRequest) {

        log.info("Received discount calculation request for customer: {}", billRequest.getCustomerId());
        try {
            DiscountResponse response = billService.processBill(billRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing bill: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Discount service is running");
    }
}