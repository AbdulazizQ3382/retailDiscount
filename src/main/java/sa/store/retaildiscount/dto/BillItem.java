package sa.store.retaildiscount.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {
    private String productName;
    private BigDecimal unitPrice;
    private Double quantity;
}