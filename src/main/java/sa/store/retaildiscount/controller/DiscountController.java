package sa.store.retaildiscount.controller;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.service.DiscountService;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    private final Logger log = org.slf4j.LoggerFactory.getLogger(DiscountController.class);

    @PostMapping("")
    public ResponseEntity<BillDTO> calculateDiscount(@RequestBody BillRequest billRequest) {

        log.info("Received discount calculation request for customer: {}", billRequest.getCustomerId());
        try {
            BillDTO response = discountService.processBill(billRequest);
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