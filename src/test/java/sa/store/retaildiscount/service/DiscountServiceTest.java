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
import sa.store.retaildiscount.dto.BillItem;
import sa.store.retaildiscount.dto.BillRequest;
import sa.store.retaildiscount.dto.CustomerDTO;
import sa.store.retaildiscount.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private List<BillItem> items;
    private CustomerDTO customer;

    @BeforeEach
    void setUp() {
        items = Arrays.asList(
            new BillItem("Gaming Laptop", new BigDecimal("1000.00"), 1.0),
            new BillItem("Wireless Mouse", new BigDecimal("50.00"), 2.0)
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
        // Affiliate gets 10% discount + $50 bulk discount = $110 + $50 = $160
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
        // Regular customer over 2 years gets 5% discount + $50 bulk discount = $55 + $50 = $105
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
        // Regular customer under 2 years gets no customer discount + $50 bulk discount = $50
        assertEquals(new BigDecimal("1045.00"), result.getNetPayableAmount());
        
        verify(mongoTemplate, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("Should process bill under $100 with no bulk discount")
    void shouldProcessBillUnder100WithNoBulkDiscount() {
        // Given
        items = Arrays.asList(
            new BillItem("Small Item", new BigDecimal("30.00"), 1.0),
            new BillItem("Another Item", new BigDecimal("40.00"), 1.0)
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
        // Employee gets 30% discount, no bulk discount = $70 - $21 = $49
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
        // No customer discount + $50 bulk discount = $50
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
}