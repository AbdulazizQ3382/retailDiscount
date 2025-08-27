package sa.store.retaildiscount.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountResponse;
import sa.store.retaildiscount.entity.Discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountCalculationService {

    private final MongoTemplate mongoTemplate;

    public DiscountResponse calculateDiscount(BillRequest billRequest) {
        log.info("Calculating discount for bill with total amount: {}", billRequest.getTotalAmount());
        
        BigDecimal originalAmount = billRequest.getTotalAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedDiscountCode = null;
        String discountDescription = null;

        // Apply customer type discount first
        BigDecimal customerTypeDiscount = applyCustomerTypeDiscount(billRequest.getCustomerType(), originalAmount);
        if (customerTypeDiscount.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = discountAmount.add(customerTypeDiscount);
            appliedDiscountCode = "CUSTOMER_TYPE_" + billRequest.getCustomerType();
            discountDescription = "Customer type discount";
        }

        // Apply discount code if provided
        if (billRequest.getDiscountCode() != null && !billRequest.getDiscountCode().isEmpty()) {
            Discount discount = findValidDiscount(billRequest.getDiscountCode());
            if (discount != null) {
                BigDecimal codeDiscount = applyDiscountCode(discount, billRequest);
                if (codeDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    discountAmount = discountAmount.add(codeDiscount);
                    appliedDiscountCode = discount.getCode();
                    discountDescription = discount.getDescription();
                }
            }
        }

        BigDecimal netAmount = originalAmount.subtract(discountAmount);
        if (netAmount.compareTo(BigDecimal.ZERO) < 0) {
            netAmount = BigDecimal.ZERO;
        }

        return DiscountResponse.builder()
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .netPayableAmount(netAmount)
                .appliedDiscountCode(appliedDiscountCode)
                .discountDescription(discountDescription)
                .build();
    }

    private BigDecimal applyCustomerTypeDiscount(String customerType, BigDecimal amount) {
        if (customerType == null) return BigDecimal.ZERO;
        
        switch (customerType.toUpperCase()) {
            case "PREMIUM":
                return amount.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
            case "VIP":
                return amount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    private Discount findValidDiscount(String discountCode) {
        Query query = new Query(Criteria.where("code").is(discountCode)
                .and("active").is(true)
                .and("startDate").lte(LocalDateTime.now())
                .and("endDate").gte(LocalDateTime.now()));
        
        return mongoTemplate.findOne(query, Discount.class);
    }

    private BigDecimal applyDiscountCode(Discount discount, BillRequest billRequest) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // Check if discount is applicable to any category in the bill
        if (discount.getApplicableCategory() != null) {
            boolean categoryMatches = billRequest.getItems().stream()
                    .anyMatch(item -> discount.getApplicableCategory().equals(item.getCategory()));
            if (!categoryMatches) {
                return BigDecimal.ZERO;
            }
        }

        // Apply percentage discount
        if (discount.getPercentage() != null) {
            discountAmount = billRequest.getTotalAmount()
                    .multiply(discount.getPercentage().divide(new BigDecimal("100")))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Apply fixed amount discount
        if (discount.getFixedAmount() != null) {
            discountAmount = discountAmount.add(discount.getFixedAmount());
        }

        return discountAmount;
    }
}