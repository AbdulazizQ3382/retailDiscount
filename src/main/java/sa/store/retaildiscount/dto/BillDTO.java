package sa.store.retaildiscount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDTO {
    private String id;
    private CustomerDTO customer;
    private List<BillItem> items;
    private BigDecimal totalAmount;
    private BigDecimal netPayableAmount;
    private LocalDateTime billDate;
    private List<DiscountDTO> discount;
}
