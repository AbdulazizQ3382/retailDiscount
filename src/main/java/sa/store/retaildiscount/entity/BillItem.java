package sa.store.retaildiscount.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillItem {

    @Id
    private String id;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
}