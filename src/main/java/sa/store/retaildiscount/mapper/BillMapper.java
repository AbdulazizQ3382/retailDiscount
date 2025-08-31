package sa.store.retaildiscount.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.DiscountDTO;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.Discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface BillMapper {

    BillMapper INSTANCE = Mappers.getMapper(BillMapper.class);


    BillDTO billEntityToBillDTO(Bill bill);

    Bill billDTOToBillEntity(BillDTO billDTO);


    DiscountDTO discountEntityToDiscountDTO(Discount discount);

    default BillDTO buildBillDTO(BillRequest billRequest, BigDecimal customerTypeDiscount, BigDecimal priceDiscount, BigDecimal totalAmount , BigDecimal... netPayableAmount) {

        var discountPerc = Stream.of(customerTypeDiscount,priceDiscount)
                .map(d -> discountAmountToPercentage(d,totalAmount)).toList();

        return BillDTO.builder()
                .customer(billRequest.getCustomer())
                .items(billRequest.getItems())
                .totalAmount(totalAmount)
                .netPayableAmount(netPayableAmount.length > 0 ? netPayableAmount[0] : totalAmount)
                .billDate(java.time.LocalDateTime.now())
                .discount(List.of(
                        DiscountDTO.builder()
                                .amount(customerTypeDiscount)
                                .type("CUSTOMER_TYPE_DISCOUNT")
                                .percentage(discountPerc.get(0))
                                .build(),
                        DiscountDTO.builder()
                                .amount(priceDiscount)
                                .type("PRICE_DISCOUNT")
                                .percentage(discountPerc.get(1))
                                .build()
                ))
                .build();
    }
    
    default String discountAmountToPercentage(BigDecimal discountAmount, BigDecimal totalAmount) {
        if(totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        return discountAmount.divide(totalAmount,2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))+"%";
    }
}