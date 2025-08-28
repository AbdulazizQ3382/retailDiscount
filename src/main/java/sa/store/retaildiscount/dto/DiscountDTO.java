package sa.store.retaildiscount.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDTO {
    private String id;
    private BigDecimal discountAmount;
    private String discountType;
}