package sa.store.retaildiscount.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "discounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discount {
    
    @Id
    private String id;
    private String code;
    private String description;
    private BigDecimal percentage;
    private BigDecimal fixedAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private String applicableCategory;
}