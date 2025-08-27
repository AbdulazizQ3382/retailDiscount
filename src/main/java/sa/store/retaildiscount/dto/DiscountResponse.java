package sa.store.retaildiscount.dto;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal netPayableAmount;
    private String appliedDiscountCode;
    private String discountDescription;
}