package sa.store.retaildiscount.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "bills")
@Data
@AllArgsConstructor
public class Bill {
    @Id
    private String id;
    private String customerId;
    private String customerType;
    private List<BillItemEntity> items;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal netPayableAmount;
    private String appliedDiscountCode;
    private String discountDescription;
    private LocalDateTime billDate;
    private List<Discount> discount;
    private String status;
}