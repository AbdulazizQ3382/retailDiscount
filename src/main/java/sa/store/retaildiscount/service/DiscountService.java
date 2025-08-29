package sa.store.retaildiscount.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountDTO;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.BillItem;
import sa.store.retaildiscount.entity.Customer;
import sa.store.retaildiscount.entity.Discount;
import sa.store.retaildiscount.mapper.BillMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final MongoTemplate mongoTemplate;

    private final CustomerService customerService;
    private final Logger log = org.slf4j.LoggerFactory.getLogger(DiscountService.class);

    @Autowired
    public DiscountService(MongoTemplate mongoTemplate, CustomerService customerService) {
        this.mongoTemplate = mongoTemplate;
        this.customerService = customerService;
    }
    public BillDTO processBill(BillRequest billRequest) {

        // Calculate discount
        BillDTO billDTO = this.calculateDiscount(billRequest);

        Bill savedBill = mongoTemplate.save(BillMapper.INSTANCE.billDTOToBill(billDTO));

        log.info("Bill saved with ID: {}", savedBill.getId());

        billDTO.setId(savedBill.getId());

        return billDTO;
    }

    public BillDTO calculateDiscount(BillRequest billRequest) {


        log.info("Calculating discount for bill with total amount: {}", billRequest);


        BigDecimal originalAmount = billRequest.getItems()
                .stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        originalAmount = originalAmount.setScale(2, RoundingMode.HALF_UP);



        BigDecimal discountAmount = originalAmount.divide(new BigDecimal("100"),2,RoundingMode.DOWN).multiply(new BigDecimal(5));


        BigDecimal customerTypeDiscount = applyCustomerTypeDiscount(billRequest.getCustomer().getCustomerType(),LocalDateTime.parse(billRequest.getCustomer().getRegistrationDate()), originalAmount);

        if(originalAmount.compareTo(new BigDecimal(100)) <= 0) {

            discountAmount = BigDecimal.ZERO;

            BigDecimal netAmount = originalAmount.subtract(customerTypeDiscount);

            return BillMapper.INSTANCE.buildBuildBillDTO(billRequest,customerTypeDiscount,discountAmount,originalAmount,netAmount);
        }

        BigDecimal netAmount = originalAmount.subtract(discountAmount.add(customerTypeDiscount));


        return BillMapper.INSTANCE.buildBuildBillDTO(billRequest, discountAmount,customerTypeDiscount,originalAmount,netAmount);
    }

    private BigDecimal applyCustomerTypeDiscount(String customerType, LocalDateTime registrationDate, BigDecimal amount) {

        if (customerType == null) return BigDecimal.ZERO;

        BigDecimal discount = switch (customerType.toUpperCase()) {
            case "EMPLOYEE" -> amount.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
            case "AFFILIATE" -> amount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };

        if (discount.compareTo(BigDecimal.ZERO) == 0) {

            if (registrationDate != null && registrationDate.isBefore(LocalDateTime.now().minusYears(2))) {
                discount = amount.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
            }
        }

        return discount;
    }
}