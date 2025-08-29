package sa.store.retaildiscount.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
public class BillRequest {
    private List<BillItemDTO> items;
    private  CustomerDTO customer;
}