package sa.store.retaildiscount.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CustomerDTO {
    private String identity;
    private String name;
    private String customerType;
    private String registrationDate;
}
