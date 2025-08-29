package sa.store.retaildiscount.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.service.BillService;
import sa.store.retaildiscount.service.DiscountService;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final DiscountService discountService;
    private final BillService billService;

    @Autowired
    public BillController(DiscountService discountService, BillService billService) {
        this.discountService = discountService;
        this.billService = billService;
    }


    @PostMapping("")
    public ResponseEntity<BillDTO> calculateDiscount(@RequestBody BillRequest billRequest) {

        //todo : add wrapper response class

        if(billRequest.getItems() == null || billRequest.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill items are required");
        }

        if(billRequest.getItems().stream().anyMatch(item -> item.getQuantity() <= 0 || item.getUnitPrice() == null || item.getUnitPrice().doubleValue() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All bill items must have positive quantity and unit price");
        }

        if(billRequest.getCustomer() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer information is required");
        }

        if(billRequest.getCustomer().getIdentity() == null || billRequest.getCustomer().getIdentity().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer identity is required");
        }

            BillDTO response = discountService.processBill(billRequest);
            return ResponseEntity.ok(response);
    }

    @GetMapping("{billId}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable String billId) {
        BillDTO bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BillDTO>> getBillsByCustomerId(@PathVariable String customerId) {
        // todo: pageable default
        List<BillDTO> bills = billService.getBillsByCustomerId(customerId);
        return ResponseEntity.ok(bills);
    }
}