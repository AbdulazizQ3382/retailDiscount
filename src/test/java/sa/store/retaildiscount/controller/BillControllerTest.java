package sa.store.retaildiscount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.config.SecurityConfig;
import sa.store.retaildiscount.dto.*;
import sa.store.retaildiscount.service.BillService;
import sa.store.retaildiscount.service.DiscountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
@Import(SecurityConfig.class) // Import your main SecurityConfig class
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscountService discountService;

    @MockitoBean
    private BillService billService;

    private ObjectMapper objectMapper;
    private BillRequest billRequest;
    private BillDTO billDTO;
    private CustomerDTO customerDTO;
    private List<BillItem> billItems;



    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test data
        customerDTO = CustomerDTO.builder()
                .identity("ID001")
                .name("John Doe")
                .customerType("EMPLOYEE")
                .registrationDate(LocalDateTime.now().minusDays(30).toString())
                .build();

        billItems = Arrays.asList(
                new BillItem("Gaming Laptop", new BigDecimal("1000.00"), 1.0),
                new BillItem("Wireless Mouse", new BigDecimal("50.00"), 2.0)
        );

        billRequest = new BillRequest();
        billRequest.setItems(billItems);
        billRequest.setCustomer(customerDTO);

        List<DiscountDTO> discounts = Arrays.asList(
                DiscountDTO.builder()
                        .discountAmount(new BigDecimal("330.00"))
                        .discountType("CUSTOMER_TYPE_DISCOUNT")
                        .build(),
                DiscountDTO.builder()
                        .discountAmount(new BigDecimal("55.00"))
                        .discountType("PRICE_DISCOUNT")
                        .build()
        );

        billDTO = BillDTO.builder()
                .id("bill123")
                .customer(customerDTO)
                .items(billItems)
                .totalAmount(new BigDecimal("1100.00"))
                .netPayableAmount(new BigDecimal("715.00"))
                .billDate(LocalDateTime.now())
                .discount(discounts)
                .build();
    }

    @Test
    @DisplayName("POST /api/bills - Should calculate discount successfully")
    void shouldCalculateDiscountSuccessfully() throws Exception {
        // Given
        when(discountService.processBill(any(BillRequest.class))).thenReturn(billDTO);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("bill123")))
                .andExpect(jsonPath("$.customer.identity", is("ID001")))
                .andExpect(jsonPath("$.customer.name", is("John Doe")))
                .andExpect(jsonPath("$.customer.customerType", is("EMPLOYEE")))
                .andExpect(jsonPath("$.totalAmount", is(1100.00)))
                .andExpect(jsonPath("$.netPayableAmount", is(715.00)))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].productName", is("Gaming Laptop")))
                .andExpect(jsonPath("$.items[0].unitPrice", is(1000.00)))
                .andExpect(jsonPath("$.items[0].quantity", is(1.0)))
                .andExpect(jsonPath("$.items[1].productName", is("Wireless Mouse")))
                .andExpect(jsonPath("$.items[1].unitPrice", is(50.00)))
                .andExpect(jsonPath("$.items[1].quantity", is(2.0)))
                .andExpect(jsonPath("$.discount", hasSize(2)))
                .andExpect(jsonPath("$.discount[0].discountAmount", is(330.00)))
                .andExpect(jsonPath("$.discount[0].discountType", is("CUSTOMER_TYPE_DISCOUNT")))
                .andExpect(jsonPath("$.discount[1].discountAmount", is(55.00)))
                .andExpect(jsonPath("$.discount[1].discountType", is("PRICE_DISCOUNT")));

        verify(discountService, times(1)).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when items are null")
    void shouldReturn400WhenItemsAreNull() throws Exception {
        // Given
        billRequest.setItems(null);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when items are empty")
    void shouldReturn400WhenItemsAreEmpty() throws Exception {
        // Given
        billRequest.setItems(Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when item has negative quantity")
    void shouldReturn400WhenItemHasNegativeQuantity() throws Exception {
        // Given
        billItems.get(0).setQuantity(-1.0);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when item has zero quantity")
    void shouldReturn400WhenItemHasZeroQuantity() throws Exception {
        // Given
        billItems.get(0).setQuantity(0.0);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when item has null unit price")
    void shouldReturn400WhenItemHasNullUnitPrice() throws Exception {
        // Given
        billItems.get(0).setUnitPrice(null);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when item has negative unit price")
    void shouldReturn400WhenItemHasNegativeUnitPrice() throws Exception {
        // Given
        billItems.get(0).setUnitPrice(new BigDecimal("-10.00"));

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when customer is null")
    void shouldReturn400WhenCustomerIsNull() throws Exception {
        // Given
        billRequest.setCustomer(null);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when customer identity is null")
    void shouldReturn400WhenCustomerIdentityIsNull() throws Exception {
        // Given
        customerDTO.setIdentity(null);

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("POST /api/bills - Should return 400 when customer identity is empty")
    void shouldReturn400WhenCustomerIdentityIsEmpty() throws Exception {
        // Given
        customerDTO.setIdentity("");

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isBadRequest());

        verify(discountService, never()).processBill(any(BillRequest.class));
    }

//    @Test
//    @DisplayName("POST /api/bills - Should return 400 with malformed JSON")
//    void shouldReturn400WithMalformedJSON() throws Exception {
//        // When & Then
//        mockMvc.perform(post("/api/bills")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{'invalid json}'"))
//                .andExpect(status().isBadRequest());
//
//        verify(discountService, never()).processBill(any(BillRequest.class));
//    }

    @Test
    @DisplayName("POST /api/bills - Should handle service exception")
    void shouldHandleServiceException() throws Exception {
        // Given
        when(discountService.processBill(any(BillRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billRequest)))
                .andExpect(status().isInternalServerError());

        verify(discountService, times(1)).processBill(any(BillRequest.class));
    }

    @Test
    @DisplayName("GET /api/bills/{billId} - Should get bill by ID successfully")
    void shouldGetBillByIdSuccessfully() throws Exception {
        // Given
        String billId = "bill123";
        when(billService.getBillById(billId)).thenReturn(billDTO);

        // When & Then
        mockMvc.perform(get("/api/bills/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("bill123")))
                .andExpect(jsonPath("$.customer.identity", is("ID001")))
                .andExpect(jsonPath("$.customer.name", is("John Doe")))
                .andExpect(jsonPath("$.totalAmount", is(1100.00)))
                .andExpect(jsonPath("$.netPayableAmount", is(715.00)))
                .andExpect(jsonPath("$.items", hasSize(2)));

        verify(billService, times(1)).getBillById(billId);
    }

    @Test
    @DisplayName("GET /api/bills/{billId} - Should return 404 when bill not found")
    void shouldReturn404WhenBillNotFound() throws Exception {
        // Given
        String billId = "nonexistent123";
        when(billService.getBillById(billId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found with ID: " + billId));

        // When & Then
        mockMvc.perform(get("/api/bills/{billId}", billId))
                .andExpect(status().isNotFound());

        verify(billService, times(1)).getBillById(billId);
    }

    @Test
    @DisplayName("GET /api/bills/customer/{customerId} - Should get bills by customer ID successfully")
    void shouldGetBillsByCustomerIdSuccessfully() throws Exception {
        // Given
        String customerId = "ID001";
        List<BillDTO> bills = Arrays.asList(billDTO);
        when(billService.getBillsByCustomerId(customerId)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/bills/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("bill123")))
                .andExpect(jsonPath("$[0].customer.identity", is("ID001")))
                .andExpect(jsonPath("$[0].customer.name", is("John Doe")))
                .andExpect(jsonPath("$[0].totalAmount", is(1100.00)))
                .andExpect(jsonPath("$[0].netPayableAmount", is(715.00)))
                .andExpect(jsonPath("$[0].items", hasSize(2)));

        verify(billService, times(1)).getBillsByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/bills/customer/{customerId} - Should get multiple bills successfully")
    void shouldGetMultipleBillsSuccessfully() throws Exception {
        // Given
        String customerId = "ID001";
        
        BillDTO billDTO2 = BillDTO.builder()
                .id("bill456")
                .customer(customerDTO)
                .items(billItems)
                .totalAmount(new BigDecimal("500.00"))
                .netPayableAmount(new BigDecimal("350.00"))
                .billDate(LocalDateTime.now().minusDays(1))
                .discount(Collections.emptyList())
                .build();

        List<BillDTO> bills = Arrays.asList(billDTO, billDTO2);
        when(billService.getBillsByCustomerId(customerId)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/bills/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("bill123")))
                .andExpect(jsonPath("$[0].totalAmount", is(1100.00)))
                .andExpect(jsonPath("$[1].id", is("bill456")))
                .andExpect(jsonPath("$[1].totalAmount", is(500.00)));

        verify(billService, times(1)).getBillsByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/bills/customer/{customerId} - Should return 404 when no bills found")
    void shouldReturn404WhenNoBillsFoundForCustomer() throws Exception {
        // Given
        String customerId = "nonexistent001";
        when(billService.getBillsByCustomerId(customerId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No bills found for customer ID: " + customerId));

        // When & Then
        mockMvc.perform(get("/api/bills/customer/{customerId}", customerId))
                .andExpect(status().isNotFound());

        verify(billService, times(1)).getBillsByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/bills/customer/{customerId} - Should handle service exception")
    void shouldHandleServiceExceptionInGetBillsByCustomerId() throws Exception {
        // Given
        String customerId = "ID001";
        when(billService.getBillsByCustomerId(customerId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/bills/customer/{customerId}", customerId))
                .andExpect(status().isInternalServerError());

        verify(billService, times(1)).getBillsByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should handle unsupported HTTP methods")
    void shouldHandleUnsupportedHttpMethods() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/bills"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/bills"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(patch("/api/bills"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Should handle requests with wrong content type")
    void shouldHandleRequestsWithWrongContentType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/bills")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Should verify request logging")
    void shouldVerifyRequestLogging() throws Exception {
        // Given
        String billId = "bill123";
        when(billService.getBillById(billId)).thenReturn(billDTO);

        // When & Then
        mockMvc.perform(get("/api/bills/{billId}", billId))
                .andExpect(status().isOk());

        // Verify service was called (logging verification would require additional setup)
        verify(billService, times(1)).getBillById(billId);
    }
}