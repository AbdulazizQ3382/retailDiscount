package sa.store.retaildiscount.entity;

import lombok.*;

import java.math.BigDecimal;


@NoArgsConstructor
@Builder
@AllArgsConstructor
@Setter
@Getter
public class Discount {

    private BigDecimal amount;
    private String type;
    private String percentage;

}