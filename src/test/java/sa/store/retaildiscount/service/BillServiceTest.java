package sa.store.retaildiscount.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.dto.BillItemDTO;
import sa.store.retaildiscount.dto.CustomerDTO;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.Customer;
import sa.store.retaildiscount.mapper.BillMapper;
import sa.store.retaildiscount.repository.BillRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private BillService billService;

    private Bill bill;
    private BillDTO billDTO;
    private Customer customer;
    private CustomerDTO customerDTO;
    private List<sa.store.retaildiscount.entity.BillItem> entityBillItems;
    private List<BillItemDTO> dtoBillItemDTOS;

    @BeforeEach
    void setUp() {
        // Setup Customer entity
        customer = new Customer();
        customer.setName("John Doe");
        customer.setIdentity("ID001");
        customer.setCustomerType("EMPLOYEE");
        customer.setRegistrationDate(LocalDateTime.now().minusDays(30));

        // Setup CustomerDTO
        customerDTO = CustomerDTO.builder()
                .identity("ID001")
                .name("John Doe")
                .customerType("EMPLOYEE")
                .registrationDate(LocalDateTime.now().minusDays(30).toString())
                .build();

        // Setup entity BillItems
        entityBillItems = Arrays.asList(
                new sa.store.retaildiscount.entity.BillItem("Gaming Laptop", new BigDecimal("1000.00"), 1),
                new sa.store.retaildiscount.entity.BillItem("Wireless Mouse", new BigDecimal("50.00"), 2)
        );

        // Setup DTO BillItems
        dtoBillItemDTOS = Arrays.asList(
                new BillItemDTO("Gaming Laptop", new BigDecimal("1000.00"), 1.0),
                new BillItemDTO("Wireless Mouse", new BigDecimal("50.00"), 2.0)
        );

        // Setup Bill entity
        bill = new Bill();
        bill.setId("bill123");
        bill.setCustomer(customer);
        bill.setItems(entityBillItems);
        bill.setTotalAmount(new BigDecimal("1100.00"));
        bill.setNetPayableAmount(new BigDecimal("770.00"));
        bill.setBillDate(LocalDateTime.now());

        // Setup BillDTO
        billDTO = BillDTO.builder()
                .id("bill123")
                .customer(customerDTO)
                .items(dtoBillItemDTOS)
                .totalAmount(new BigDecimal("1100.00"))
                .netPayableAmount(new BigDecimal("770.00"))
                .billDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get bill by ID successfully")
    void shouldGetBillByIdSuccessfully() {
        // Given
        String billId = "bill123";
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billMapper.billEntityToBillDTO(bill)).thenReturn(billDTO);

        // When
        BillDTO result = billService.getBillById(billId);

        // Then
        assertNotNull(result);
        assertEquals("bill123", result.getId());
        assertEquals("John Doe", result.getCustomer().getName());
        assertEquals("ID001", result.getCustomer().getIdentity());
        assertEquals(new BigDecimal("1100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("770.00"), result.getNetPayableAmount());

        verify(billRepository, times(1)).findById(billId);
        verify(billMapper, times(1)).billEntityToBillDTO(bill);
    }

    @Test
    @DisplayName("Should throw ResponseStatusException when bill not found by ID")
    void shouldThrowExceptionWhenBillNotFoundById() {
        // Given
        String billId = "nonexistent123";
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            billService.getBillById(billId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Bill not found with ID: nonexistent123", exception.getReason());

        verify(billRepository, times(1)).findById(billId);
        verify(billMapper, never()).billEntityToBillDTO(any(Bill.class));
    }

    @Test
    @DisplayName("Should get bills by customer ID successfully")
    void shouldGetBillsByCustomerIdSuccessfully() {
        // Given
        String customerIdentity = "ID001";
        List<Bill> bills = Arrays.asList(bill);
        List<BillDTO> expectedBillDTOs = Arrays.asList(billDTO);

        when(billRepository.findByCustomer_Identity(customerIdentity)).thenReturn(bills);
        when(billMapper.billEntityToBillDTO(bill)).thenReturn(billDTO);

        // When
        List<BillDTO> result = billService.getBillsByCustomerId(customerIdentity);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("bill123", result.get(0).getId());
        assertEquals("ID001", result.get(0).getCustomer().getIdentity());
        assertEquals(new BigDecimal("1100.00"), result.get(0).getTotalAmount());

        verify(billRepository, times(1)).findByCustomer_Identity(customerIdentity);
        verify(billMapper, times(1)).billEntityToBillDTO(bill);
    }

    @Test
    @DisplayName("Should get multiple bills by customer ID successfully")
    void shouldGetMultipleBillsByCustomerIdSuccessfully() {
        // Given
        String customerIdentity = "ID001";
        
        Bill bill2 = new Bill();
        bill2.setId("bill456");
        bill2.setCustomer(customer);
        bill2.setItems(entityBillItems);
        bill2.setTotalAmount(new BigDecimal("500.00"));
        bill2.setNetPayableAmount(new BigDecimal("350.00"));
        bill2.setBillDate(LocalDateTime.now().minusDays(1));

        BillDTO billDTO2 = BillDTO.builder()
                .id("bill456")
                .customer(customerDTO)
                .items(dtoBillItemDTOS)
                .totalAmount(new BigDecimal("500.00"))
                .netPayableAmount(new BigDecimal("350.00"))
                .billDate(LocalDateTime.now().minusDays(1))
                .build();

        List<Bill> bills = Arrays.asList(bill, bill2);

        when(billRepository.findByCustomer_Identity(customerIdentity)).thenReturn(bills);
        when(billMapper.billEntityToBillDTO(bill)).thenReturn(billDTO);
        when(billMapper.billEntityToBillDTO(bill2)).thenReturn(billDTO2);

        // When
        List<BillDTO> result = billService.getBillsByCustomerId(customerIdentity);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        assertEquals("bill123", result.get(0).getId());
        assertEquals("bill456", result.get(1).getId());
        
        assertEquals(new BigDecimal("1100.00"), result.get(0).getTotalAmount());
        assertEquals(new BigDecimal("500.00"), result.get(1).getTotalAmount());

        verify(billRepository, times(1)).findByCustomer_Identity(customerIdentity);
        verify(billMapper, times(2)).billEntityToBillDTO(any(Bill.class));
    }

    @Test
    @DisplayName("Should throw ResponseStatusException when no bills found for customer ID")
    void shouldThrowExceptionWhenNoBillsFoundForCustomerId() {
        // Given
        String customerIdentity = "nonexistent001";
        when(billRepository.findByCustomer_Identity(customerIdentity)).thenReturn(Collections.emptyList());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            billService.getBillsByCustomerId(customerIdentity);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No bills found for customer ID: nonexistent001", exception.getReason());

        verify(billRepository, times(1)).findByCustomer_Identity(customerIdentity);
        verify(billMapper, never()).billEntityToBillDTO(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle repository exception in getBillById")
    void shouldHandleRepositoryExceptionInGetBillById() {
        // Given
        String billId = "bill123";
        when(billRepository.findById(billId)).thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            billService.getBillById(billId);
        });

        verify(billRepository, times(1)).findById(billId);
        verify(billMapper, never()).billEntityToBillDTO(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle repository exception in getBillsByCustomerId")
    void shouldHandleRepositoryExceptionInGetBillsByCustomerId() {
        // Given
        String customerIdentity = "ID001";
        when(billRepository.findByCustomer_Identity(customerIdentity))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            billService.getBillsByCustomerId(customerIdentity);
        });

        verify(billRepository, times(1)).findByCustomer_Identity(customerIdentity);
        verify(billMapper, never()).billEntityToBillDTO(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle mapper exception")
    void shouldHandleMapperException() {
        // Given
        String billId = "bill123";
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billMapper.billEntityToBillDTO(bill)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            billService.getBillById(billId);
        });

        verify(billRepository, times(1)).findById(billId);
        verify(billMapper, times(1)).billEntityToBillDTO(bill);
    }

    @Test
    @DisplayName("Should verify correct method interactions")
    void shouldVerifyCorrectMethodInteractions() {
        // Given
        String billId = "bill123";
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billMapper.billEntityToBillDTO(bill)).thenReturn(billDTO);

        // When
        BillDTO result = billService.getBillById(billId);

        // Then
        verify(billRepository).findById(eq(billId));
        verify(billMapper).billEntityToBillDTO(eq(bill));
        verifyNoMoreInteractions(billRepository, billMapper);
        
        assertNotNull(result);
        assertEquals(billDTO.getId(), result.getId());
    }

    @Test
    @DisplayName("Should verify correct parameter passing to repository methods")
    void shouldVerifyCorrectParameterPassingToRepositoryMethods() {
        // Given
        String customerIdentity = "TEST_ID_001";
        List<Bill> bills = Arrays.asList(bill);
        when(billRepository.findByCustomer_Identity(customerIdentity)).thenReturn(bills);
        when(billMapper.billEntityToBillDTO(any(Bill.class))).thenReturn(billDTO);

        // When
        List<BillDTO> result = billService.getBillsByCustomerId(customerIdentity);

        // Then
        verify(billRepository).findByCustomer_Identity(eq(customerIdentity));
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}