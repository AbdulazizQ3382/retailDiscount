package sa.store.retaildiscount.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.BillItemEntity;
import sa.store.retaildiscount.entity.Customer;
import sa.store.retaildiscount.entity.Discount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class MongoInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;
    private final Logger log = LoggerFactory.getLogger(MongoInitializer.class);

    @Autowired
    public MongoInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting MongoDB collections and documents initialization...");
        
        initializeCustomers();
        initializeDiscounts();
        initializeBills();
        
        log.info("MongoDB initialization completed!");
    }

    private void initializeCustomers() {
        if (mongoTemplate.count(new Query(), Customer.class) == 0) {
            log.info("Creating customers collection and inserting sample documents...");
            
            List<Customer> customers = Arrays.asList(
                new Customer(null, "Ahmed", "Al-Mahmoud", "ahmed.mahmoud@email.com", "+966501234567", "PREMIUM", LocalDateTime.now().minusDays(30), true),
                new Customer(null, "Fatima", "Al-Zahra", "fatima.zahra@email.com", "+966501234568", "VIP", LocalDateTime.now().minusDays(15), true),
                new Customer(null, "Mohammed", "Al-Rashid", "mohammed.rashid@email.com", "+966501234569", "REGULAR", LocalDateTime.now().minusDays(20), true),
                new Customer(null, "Aisha", "Al-Nouri", "aisha.nouri@email.com", "+966501234570", "PREMIUM", LocalDateTime.now().minusDays(10), true),
                new Customer(null, "Omar", "Al-Farisi", "omar.farisi@email.com", "+966501234571", "REGULAR", LocalDateTime.now().minusDays(5), true)
            );
            
            mongoTemplate.insertAll(customers);
            log.info("Inserted {} customer documents", customers.size());
        } else {
            log.info("Customers collection already exists with data");
        }
    }

    private void initializeDiscounts() {
        if (mongoTemplate.count(new Query(), Discount.class) == 0) {
            log.info("Creating discounts collection and inserting sample documents...");
            
            List<Discount> discounts = Arrays.asList(
                new Discount(null, "ELECTRONICS10", "10% discount on all electronics", new BigDecimal("10"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), true, "Electronics", "PERCENTAGE"),
                new Discount(null, "CLOTHING20", "20% discount on clothing items", new BigDecimal("20"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(15), true, "Clothing", "PERCENTAGE"),
                new Discount(null, "APPLIANCES15", "15% discount on home appliances", new BigDecimal("15"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(45), true, "Appliances", "PERCENTAGE"),
                new Discount(null, "NEWUSER50", "50 SAR off for new customers", null, new BigDecimal("50"),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(60), true, null, "FIXED"),
                new Discount(null, "SUMMER25", "25% summer sale discount", new BigDecimal("25"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(90), true, null, "PERCENTAGE"),
                new Discount(null, "BOOKS10", "10% discount on books", new BigDecimal("10"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(20), true, "Books", "PERCENTAGE")
            );
            
            mongoTemplate.insertAll(discounts);
            log.info("Inserted {} discount documents", discounts.size());
        } else {
            log.info("Discounts collection already exists with data");
        }
    }

    private void initializeBills() {
        if (mongoTemplate.count(new Query(), Bill.class) == 0) {
            log.info("Creating bills collection and inserting sample documents...");
            
            // Create sample discounts for nested data
            Discount electronicsDiscount = new Discount(null, "ELECTRONICS10", "10% discount on all electronics", 
                new BigDecimal("10"), null, LocalDateTime.now(), LocalDateTime.now().plusDays(30), true, "Electronics", "PERCENTAGE");
            
            Discount clothingDiscount = new Discount(null, "CLOTHING20", "20% discount on clothing items", 
                new BigDecimal("20"), null, LocalDateTime.now(), LocalDateTime.now().plusDays(15), true, "Clothing", "PERCENTAGE");
            
            Discount fixedDiscount = new Discount(null, "NEWUSER50", "50 SAR off for new customers", 
                null, new BigDecimal("50"), LocalDateTime.now(), LocalDateTime.now().plusDays(60), true, null, "FIXED");

            List<Bill> bills = Arrays.asList(
                // Bill 1 - Electronics purchase with discount
                new Bill(null, "1", "PREMIUM", Arrays.asList(
                    new BillItemEntity("prod1", "Gaming Laptop", "Electronics", new BigDecimal("2500.00"), 1, new BigDecimal("2500.00")),
                    new BillItemEntity("prod2", "Wireless Mouse", "Electronics", new BigDecimal("150.00"), 2, new BigDecimal("300.00"))
                ), new BigDecimal("2800.00"), new BigDecimal("280.00"), new BigDecimal("2520.00"), "ELECTRONICS10", 
                "10% discount on all electronics", LocalDateTime.now().minusDays(3), 
                Arrays.asList(electronicsDiscount), "COMPLETED"),
                
                // Bill 2 - Clothing purchase with multiple discounts  
                new Bill(null, "2", "VIP", Arrays.asList(
                    new BillItemEntity("prod3", "Designer Shirt", "Clothing", new BigDecimal("299.99"), 2, new BigDecimal("599.98")),
                    new BillItemEntity("prod4", "Premium Jeans", "Clothing", new BigDecimal("199.99"), 1, new BigDecimal("199.99"))
                ), new BigDecimal("799.97"), new BigDecimal("159.99"), new BigDecimal("639.98"), "CLOTHING20", 
                "20% discount on clothing items", LocalDateTime.now().minusDays(2), 
                Arrays.asList(clothingDiscount), "COMPLETED"),
                
                // Bill 3 - Mixed items with fixed discount
                new Bill(null, "3", "REGULAR", Arrays.asList(
                    new BillItemEntity("prod5", "Coffee Maker", "Appliances", new BigDecimal("450.00"), 1, new BigDecimal("450.00")),
                    new BillItemEntity("prod6", "Coffee Beans", "Food", new BigDecimal("75.00"), 3, new BigDecimal("225.00"))
                ), new BigDecimal("675.00"), new BigDecimal("50.00"), new BigDecimal("625.00"), "NEWUSER50", 
                "50 SAR off for new customers", LocalDateTime.now().minusDays(1), 
                Arrays.asList(fixedDiscount), "COMPLETED"),
                
                // Bill 4 - Large order with multiple discount types
                new Bill(null, "4", "VIP", Arrays.asList(
                    new BillItemEntity("prod7", "Smart TV", "Electronics", new BigDecimal("1200.00"), 1, new BigDecimal("1200.00")),
                    new BillItemEntity("prod8", "Sound Bar", "Electronics", new BigDecimal("300.00"), 1, new BigDecimal("300.00")),
                    new BillItemEntity("prod9", "Casual Wear Set", "Clothing", new BigDecimal("180.00"), 2, new BigDecimal("360.00"))
                ), new BigDecimal("1860.00"), new BigDecimal("186.00"), new BigDecimal("1674.00"), "ELECTRONICS10", 
                "Multiple discounts applied", LocalDateTime.now(), 
                Arrays.asList(electronicsDiscount, clothingDiscount), "COMPLETED")
            );
            
            mongoTemplate.insertAll(bills);
            log.info("Inserted {} bill documents with nested items and discounts", bills.size());
        } else {
            log.info("Bills collection already exists with data");
        }
    }
}