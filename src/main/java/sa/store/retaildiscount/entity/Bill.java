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
@NoArgsConstructor
public class Bill {
    @Id
    private String id;
    private Customer customer;
    private List<BillItem> items;
    private BigDecimal totalAmount;
    private BigDecimal netPayableAmount;
    private LocalDateTime billDate;
    private List<Discount> discount;
}