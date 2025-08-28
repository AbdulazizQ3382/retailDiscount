package sa.store.retaildiscount.controller;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.service.BillService;
import sa.store.retaildiscount.service.DiscountService;

import java.util.List;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final DiscountService discountService;
    private final BillService billService;

    @Autowired
    public DiscountController(DiscountService discountService, BillService billService) {
        this.discountService = discountService;
        this.billService = billService;
    }

    private final Logger log = org.slf4j.LoggerFactory.getLogger(DiscountController.class);

    @PostMapping("")
    public ResponseEntity<BillDTO> calculateDiscount(@RequestBody BillRequest billRequest) {

        //todo : add wrapper response class

        log.info("Received discount calculation request for customer: {}", billRequest.getCustomerId());
            BillDTO response = discountService.processBill(billRequest);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable String billId) {
        log.info("Received request to get bill with ID: {}", billId);
        BillDTO bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/bills/customer/{customerId}")
    public ResponseEntity<List<BillDTO>> getBillsByCustomerId(@PathVariable String customerId) {
        log.info("Received request to get bills for customer ID: {}", customerId);
        List<BillDTO> bills = billService.getBillsByCustomerId(customerId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Discount service is running");
    }
}