package sa.store.retaildiscount.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class BillRequest {
    private String customerId;
    private String customerType;
    private List<BillItem> items;
    private BigDecimal totalAmount;
    private String discountCode;
}