package sa.store.retaildiscount.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountDTO;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.Discount;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BillMapper {

    BillMapper INSTANCE = Mappers.getMapper(BillMapper.class);


    BillDTO billEntityToBillDTO(Bill bill);

    Bill billDTOToBill(BillDTO billDTO);


    DiscountDTO discountEntityToDiscountDTO(Discount discount);

    default BillDTO buildBillDTO(BillRequest billRequest, BigDecimal customerTypeDiscount, BigDecimal priceDiscount, BigDecimal totalAmount , BigDecimal... netPayableAmount) {
        return BillDTO.builder()
                .customer(billRequest.getCustomer())
                .items(billRequest.getItems())
                .totalAmount(totalAmount)
                .netPayableAmount(netPayableAmount.length > 0 ? netPayableAmount[0] : totalAmount)
                .billDate(java.time.LocalDateTime.now())
                .discount(List.of(
                        DiscountDTO.builder()
                                .discountAmount(customerTypeDiscount)
                                .discountType("CUSTOMER_TYPE_DISCOUNT")
                                .build(),
                        DiscountDTO.builder()
                                .discountAmount(priceDiscount)
                                .discountType("PRICE_DISCOUNT")
                                .build()
                ))
                .build();
    }
}