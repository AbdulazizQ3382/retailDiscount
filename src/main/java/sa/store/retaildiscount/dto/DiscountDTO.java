package sa.store.retaildiscount.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDTO {
    private BigDecimal amount;
    private String type;
    private String percentage;
}