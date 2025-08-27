package sa.store.retaildiscount.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import sa.store.retaildiscount.dto.BillItem;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountResponse;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.BillItemEntity;
import sa.store.retaildiscount.entity.Discount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final MongoTemplate mongoTemplate;
    private final DiscountCalculationService discountCalculationService;

    private final Logger log = org.slf4j.LoggerFactory.getLogger(BillService.class);

    @Autowired
    public BillService(MongoTemplate mongoTemplate, DiscountCalculationService discountCalculationService) {
        this.mongoTemplate = mongoTemplate;
        this.discountCalculationService = discountCalculationService;
    }
    public DiscountResponse processBill(BillRequest billRequest) {
        log.info("Processing bill for customer: {}", billRequest.getCustomerId());
        
        // Calculate discount
        DiscountResponse discountResponse = discountCalculationService.calculateDiscount(billRequest);
        
        // Save bill to database
        Bill bill = createBillFromRequest(billRequest, discountResponse);
        Bill savedBill = mongoTemplate.save(bill);


        
        log.info("Bill saved with ID: {}", savedBill.getId());
        
        return discountResponse;
    }

    private Bill createBillFromRequest(BillRequest billRequest, DiscountResponse discountResponse) {
        List<BillItemEntity> billItems = billRequest.getItems().stream()
                .map(this::convertToBillItemEntity)
                .collect(Collectors.toList());


        return new Bill(
                null,
                billRequest.getCustomerId(),
                billRequest.getCustomerType(),
                billItems,
                discountResponse.getOriginalAmount(),
                discountResponse.getDiscountAmount(),
                discountResponse.getNetPayableAmount(),
                discountResponse.getAppliedDiscountCode(),
                discountResponse.getDiscountDescription(),
                LocalDateTime.now(),
                List.of(new Discount(null,"new discount","new discount",discountResponse.getDiscountAmount(),discountResponse.getDiscountAmount(),LocalDateTime.now(),LocalDateTime.now(),true,"SYSTEM","SYSTEM"))
                ,
                "COMPLETED"
        );
    }

    private BillItemEntity convertToBillItemEntity(BillItem billItem) {
        return new BillItemEntity(
                billItem.getProductId(),
                billItem.getProductName(),
                billItem.getCategory(),
                billItem.getUnitPrice(),
                billItem.getQuantity(),
                billItem.getItemTotal()
        );
    }
}