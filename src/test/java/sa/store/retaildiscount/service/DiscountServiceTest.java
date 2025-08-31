package sa.store.retaildiscount.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillItemDTO;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.CustomerDTO;
import sa.store.retaildiscount.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DiscountService discountService;

    private BillRequest billRequest;
    private List<BillItemDTO> items;
    private CustomerDTO customer;

    @BeforeEach
    void setUp() {
        items = Arrays.asList(
            new BillItemDTO("Gaming Laptop", new BigDecimal("1000.00"), 1.0),
            new BillItemDTO("Wireless Mouse", new BigDecimal("50.00"), 2.0)
        );
        
        customer = CustomerDTO.builder()
            .identity("ID001")
            .name("John Doe")
            .customerType("EMPLOYEE")
            .registrationDate(LocalDateTime.now().minusDays(30).toString())
            .build();
            
        billRequest = new BillRequest();
        billRequest.setItems(items);
        billRequest.setCustomer(customer);


    }

    @Test
    @DisplayName("Should process bill successfully for employee customer")
    void shouldProcessBillSuccessfullyForEmployeeCustomer() {
        Bill savedBill = new Bill();
        savedBill.setId("generatedId123");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("generatedId123", result.getId());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("715.00"), result.getNetPayableAmount());
        
        // Verify MongoTemplate.save was called once
        verify(mongoTemplate, times(1)).save(any(Bill.class));
        
        // Verify the saved bill data
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(mongoTemplate).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        
        assertNotNull(capturedBill);
        assertEquals(new BigDecimal("1100.00"), capturedBill.getTotalAmount());
        assertEquals(new BigDecimal("715.00"), capturedBill.getNetPayableAmount());
    }

    @Test
    @DisplayName("Should process bill with affiliate customer discount")
    void shouldProcessBillWithAffiliateCustomerDiscount() {
        // Given
        customer.setCustomerType("AFFILIATE");
        Bill savedBill = new Bill();
        savedBill.setId("affiliateId123");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("affiliateId123", result.getId());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("935.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should process bill with regular customer over 2 years")
    void shouldProcessBillWithRegularCustomerOver2Years() {
        // Given
        customer.setCustomerType("REGULAR");
        customer.setRegistrationDate(LocalDateTime.now().minusYears(3).toString());
        Bill savedBill = new Bill();
        savedBill.setId("regularId123");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("regularId123", result.getId());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("990.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should process bill with regular customer under 2 years")
    void shouldProcessBillWithRegularCustomerUnder2Years() {
        // Given
        customer.setCustomerType("REGULAR");
        customer.setRegistrationDate(LocalDateTime.now().minusMonths(6).toString());
        Bill savedBill = new Bill();
        savedBill.setId("regularNewId123");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("regularNewId123", result.getId());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("1045.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should process bill under $100 with no bulk discount")
    void shouldProcessBillUnder100WithNoBulkDiscount() {
        // Given
        items = Arrays.asList(
            new BillItemDTO("Small Item", new BigDecimal("30.00"), 1.0),
            new BillItemDTO("Another Item", new BigDecimal("40.00"), 1.0)
        );
        billRequest.setItems(items);
        
        Bill savedBill = new Bill();
        savedBill.setId("smallBillId123");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("smallBillId123", result.getId());
        assertEquals(new BigDecimal("70.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("49.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle null customer type")
    void shouldHandleNullCustomerType() {
        // Given
        customer.setCustomerType(null);
        customer.setRegistrationDate(LocalDateTime.now().minusMonths(6).toString());
        Bill savedBill = new Bill();
        savedBill.setId("nullCustomerTypeId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("nullCustomerTypeId", result.getId());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("1045.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle empty items list")
    void shouldHandleEmptyItemsList() {

        billRequest.setItems(Arrays.asList());
        Bill savedBill = new Bill();
        savedBill.setId("emptyItemsId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        assertNotNull(result);
        assertEquals("emptyItemsId", result.getId());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should verify MongoTemplate interaction")
    void shouldVerifyMongoTemplateInteraction() {
        // Given
        Bill savedBill = new Bill();
        savedBill.setId("interactionTestId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(billRequest);

        // Then
        verify(mongoTemplate, times(1)).save(any(Bill.class));
        
        // Verify the argument passed to save method
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(mongoTemplate).save(billCaptor.capture());
        
        Bill capturedBill = billCaptor.getValue();
        assertNotNull(capturedBill);
        assertNotNull(capturedBill.getCustomer());
        assertEquals("John Doe", capturedBill.getCustomer().getName());
        assertEquals("EMPLOYEE", capturedBill.getCustomer().getCustomerType());
        assertNotNull(capturedBill.getItems());
        assertEquals(2, capturedBill.getItems().size());
    }

    @Test
    @DisplayName("Should handle MongoTemplate exception")
    void shouldHandleMongoTemplateException() {
        // Given
        when(mongoTemplate.save(any(Bill.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            discountService.processBill(billRequest);
        });
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should process large bill with complex example data")
    void shouldProcessLargeBillWithComplexExampleData() {
        // Given - Complex example as provided
        List<BillItemDTO> complexItems = Arrays.asList(
            new BillItemDTO("Gaming Laptop", new BigDecimal("894430.00"), 4.0),
            new BillItemDTO("Wireless Mouse", new BigDecimal("4994.00"), 4.3333),
            new BillItemDTO("USB Cable", new BigDecimal("4434.50"), 1.24345),
            new BillItemDTO("USB Cable2", new BigDecimal("4434.50"), 1.24345),
            new BillItemDTO("USB Cable4", new BigDecimal("4434.50"), 1.24345)
        );

        CustomerDTO complexCustomer = CustomerDTO.builder()
            .identity("101010103434566")
            .name("Abdulaziz Qannam")
            .customerType("FFFF")  // Unknown customer type
            .registrationDate("2025-01-11T10:04:23.232")
            .build();

        BillRequest complexBillRequest = new BillRequest();
        complexBillRequest.setItems(complexItems);
        complexBillRequest.setCustomer(complexCustomer);

        Bill savedBill = new Bill();
        savedBill.setId("complexBillId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(complexBillRequest);

        // Then
        assertNotNull(result);
        assertEquals("complexBillId", result.getId());

        BigDecimal expectedTotal = new BigDecimal("3615902.74");

        BigDecimal expectedNetPayableAmount = new BigDecimal("3435107.74");

        assertEquals(expectedTotal,result.getTotalAmount());
        assertEquals(expectedNetPayableAmount,result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle high precision quantities correctly")
    void shouldHandleHighPrecisionQuantitiesCorrectly() {
        // Given
        List<BillItemDTO> precisionItems = Arrays.asList(
            new BillItemDTO("Precision Item 1", new BigDecimal("4434.50453"), 1.24345),
            new BillItemDTO("Precision Item 2", new BigDecimal("9994.55399"), 2.6789),
            new BillItemDTO("Precision Item 3", new BigDecimal("1500.5375"), 0.333)
        );

        CustomerDTO precisionCustomer = CustomerDTO.builder()
            .identity("PRECISION001")
            .name("Precision Test Customer")
            .customerType("EMPLOYEE")
            .registrationDate(LocalDateTime.now().minusDays(100).toString())
            .build();

        BillRequest precisionBillRequest = new BillRequest();
        precisionBillRequest.setItems(precisionItems);
        precisionBillRequest.setCustomer(precisionCustomer);

        Bill savedBill = new Bill();
        savedBill.setId("precisionBillId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(precisionBillRequest);

        // Then
        assertNotNull(result);

        BigDecimal expectedTotal = new BigDecimal("32788.17");

        BigDecimal expectedNetPayableAmount = new BigDecimal("21316.72");

        assertEquals(expectedTotal,result.getTotalAmount());
        assertEquals(expectedNetPayableAmount,result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }


    @Test
    @DisplayName("Should handle edge case with very small amounts")
    void shouldHandleEdgeCaseWithVerySmallAmounts() {
        // Given
        List<BillItemDTO> smallItems = Arrays.asList(
            new BillItemDTO("Penny Item", new BigDecimal("0.01"), 1.0),
            new BillItemDTO("Nickel Item", new BigDecimal("0.05"), 2.0),
            new BillItemDTO("Dime Item", new BigDecimal("0.10"), 5.0)
        );

        CustomerDTO smallCustomer = CustomerDTO.builder()
            .identity("SMALL001")
            .name("Small Purchase Customer")
            .customerType("EMPLOYEE")
            .registrationDate(LocalDateTime.now().minusYears(5).toString())
            .build();

        BillRequest smallBillRequest = new BillRequest();
        smallBillRequest.setItems(smallItems);
        smallBillRequest.setCustomer(smallCustomer);

        Bill savedBill = new Bill();
        savedBill.setId("smallBillId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(smallBillRequest);

        // Then
        assertNotNull(result);
        assertEquals("smallBillId", result.getId());
        
        // Total = 0.01 + 0.10 + 0.50 = 0.61
        BigDecimal expectedTotal = new BigDecimal("0.61");
        assertEquals(0, result.getTotalAmount().compareTo(expectedTotal));
        
        // Employee gets 30% discount, no bulk discount (under $100)
        BigDecimal expectedNet = expectedTotal.multiply(new BigDecimal("0.70"));
        assertTrue(Math.abs(result.getNetPayableAmount().subtract(expectedNet).doubleValue()) < 0.01);
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should throw DateTimeParseException for invalid registration date")
    void shouldThrowDateTimeParseExceptionForInvalidRegistrationDate() {
        // Given
        CustomerDTO invalidDateCustomer = CustomerDTO.builder()
            .identity("INVALID001")
            .name("Invalid Date Customer")
            .customerType("REGULAR")
            .registrationDate("2023-13-40T25:61:61")  // Invalid date format
            .build();

        BillRequest invalidDateBillRequest = new BillRequest();
        invalidDateBillRequest.setItems(items);
        invalidDateBillRequest.setCustomer(invalidDateCustomer);

        // When & Then
        assertThrows(DateTimeParseException.class, () -> {
            discountService.processBill(invalidDateBillRequest);
        });

        // Verify that save was never called due to exception
        verify(mongoTemplate, never()).save(any(Bill.class));
    }


    @Test
    @DisplayName("Should handle bill with more than 20 items")
    void shouldHandleBillWithMoreThan20Items() {
        // Given - Create 25 bill items
        List<BillItemDTO> manyItems = Arrays.asList(
            new BillItemDTO("Item 1", new BigDecimal("50.00"), 1.0),
            new BillItemDTO("Item 2", new BigDecimal("75.50"), 2.0),
            new BillItemDTO("Item 3", new BigDecimal("100.25"), 1.5),
            new BillItemDTO("Item 4", new BigDecimal("25.99"), 3.0),
            new BillItemDTO("Item 5", new BigDecimal("200.00"), 1.0),
            new BillItemDTO("Item 6", new BigDecimal("45.75"), 2.5),
            new BillItemDTO("Item 7", new BigDecimal("150.00"), 1.0),
            new BillItemDTO("Item 8", new BigDecimal("89.99"), 1.0),
            new BillItemDTO("Item 9", new BigDecimal("120.50"), 2.0),
            new BillItemDTO("Item 10", new BigDecimal("65.25"), 1.0),
            new BillItemDTO("Item 11", new BigDecimal("300.00"), 1.0),
            new BillItemDTO("Item 12", new BigDecimal("15.99"), 4.0),
            new BillItemDTO("Item 13", new BigDecimal("85.75"), 1.0),
            new BillItemDTO("Item 14", new BigDecimal("110.00"), 2.0),
            new BillItemDTO("Item 15", new BigDecimal("40.50"), 3.0),
            new BillItemDTO("Item 16", new BigDecimal("95.25"), 1.0),
            new BillItemDTO("Item 17", new BigDecimal("175.00"), 1.0),
            new BillItemDTO("Item 18", new BigDecimal("55.99"), 2.0),
            new BillItemDTO("Item 19", new BigDecimal("80.00"), 1.5),
            new BillItemDTO("Item 20", new BigDecimal("125.75"), 1.0),
            new BillItemDTO("Item 21", new BigDecimal("35.50"), 2.0),
            new BillItemDTO("Item 22", new BigDecimal("145.25"), 1.0),
            new BillItemDTO("Item 23", new BigDecimal("90.00"), 1.0),
            new BillItemDTO("Item 24", new BigDecimal("60.75"), 2.0),
            new BillItemDTO("Item 25", new BigDecimal("180.99"), 1.0)
        );

        CustomerDTO manyItemsCustomer = CustomerDTO.builder()
            .identity("MANY001")
            .name("Many Items Customer")
            .customerType("EMPLOYEE")
            .registrationDate(LocalDateTime.now().minusYears(1).toString())
            .build();

        BillRequest manyItemsBillRequest = new BillRequest();
        manyItemsBillRequest.setItems(manyItems);
        manyItemsBillRequest.setCustomer(manyItemsCustomer);

        Bill savedBill = new Bill();
        savedBill.setId("manyItemsBillId");
        when(mongoTemplate.save(any(Bill.class))).thenReturn(savedBill);

        // When
        BillDTO result = discountService.processBill(manyItemsBillRequest);

        // Then
        assertNotNull(result);
        assertEquals("manyItemsBillId", result.getId());


        BigDecimal totalAmount = result.getTotalAmount();
        BigDecimal customerDiscount = totalAmount.multiply(new BigDecimal("0.30"));
        BigDecimal bulkDiscount = new BigDecimal(Math.floor(totalAmount.divide(new BigDecimal("100")).doubleValue())* 5);
        BigDecimal expectedNet = totalAmount.subtract(customerDiscount).subtract(bulkDiscount);
        
        assertEquals(0, result.getNetPayableAmount().compareTo(expectedNet));
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
        
        // Verify that all items are processed
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(mongoTemplate).save(billCaptor.capture());
        Bill capturedBill = billCaptor.getValue();
        
        assertNotNull(capturedBill.getItems());
        assertEquals(25, capturedBill.getItems().size());
    }
}