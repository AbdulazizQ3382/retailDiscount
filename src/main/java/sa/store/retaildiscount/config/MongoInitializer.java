package sa.store.retaildiscount.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.entity.BillItem;
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
//        initializeDiscounts();
        initializeBills();
        
        log.info("MongoDB initialization completed!");
    }

    private void initializeCustomers() {
        if (mongoTemplate.count(new Query(), Customer.class) == 0) {
            log.info("Creating customers collection and inserting sample documents...");
            
            List<Customer> customers = Arrays.asList(
                new Customer(null, "Ahmed", "Al-Mahmoud", "ahmed.mahmoud@email.com", "+966501234567", "EMPLOYEE", LocalDateTime.now().minusDays(30)),
                new Customer(null, "Fatima", "Al-Zahra", "fatima.zahra@email.com", "+966501234568", "EMPLOYEE", LocalDateTime.now().minusDays(15)),
                new Customer(null, "Mohammed", "Al-Rashid", "mohammed.rashid@email.com", "+966501234569", "AFFILIATE", LocalDateTime.now().minusDays(20)),
                new Customer(null, "Aisha", "Al-Nouri", "aisha.nouri@email.com", "+966501234570", "AFFILIATE", LocalDateTime.now().minusDays(10)),
                new Customer(null, "Omar", "Al-Farisi", "omar.farisi@email.com", "+966501234571", "REGULAR", LocalDateTime.now().minusDays(5))
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
                new Discount(null, new BigDecimal("10"), "DISCOUNT_PR"),
                new Discount(null, new BigDecimal("20"), "PERCENTAGE"),
                new Discount(null, new BigDecimal("15"), "PERCENTAGE"),
                new Discount(null, new BigDecimal("50"), "FIXED"),
                new Discount(null, new BigDecimal("25"), "PERCENTAGE"),
                new Discount(null, new BigDecimal("10"), "PERCENTAGE")
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
            Discount electronicsDiscount = new Discount(null, new BigDecimal("10"), "CUSTOMER_TYPE_DISCOUNT");
            Discount clothingDiscount = new Discount(null, new BigDecimal("20"), "CUSTOMER_TYPE_DISCOUNT");
            Discount fixedDiscount = new Discount(null, new BigDecimal("50"), "PRICE_DISCOUNT");

            List<Bill> bills = Arrays.asList(
                new Bill(null, "customer1", Arrays.asList(
                    new BillItem(null, "Gaming Laptop", new BigDecimal("2500.00"), 1),
                    new BillItem(null, "Wireless Mouse", new BigDecimal("150.00"), 2)
                ), new BigDecimal("2800.00"), new BigDecimal("2520.00"), LocalDateTime.now().minusDays(3), 
                Arrays.asList(electronicsDiscount)),
                
                new Bill(null, "customer2", Arrays.asList(
                    new BillItem(null, "Designer Shirt", new BigDecimal("299.99"), 2),
                    new BillItem(null, "Premium Jeans", new BigDecimal("199.99"), 1)
                ), new BigDecimal("799.97"), new BigDecimal("639.98"), LocalDateTime.now().minusDays(2), 
                Arrays.asList(clothingDiscount)),
                
                new Bill(null, "customer3", Arrays.asList(
                    new BillItem(null, "Coffee Maker", new BigDecimal("450.00"), 1),
                    new BillItem(null, "Coffee Beans", new BigDecimal("75.00"), 3)
                ), new BigDecimal("675.00"), new BigDecimal("625.00"), LocalDateTime.now().minusDays(1), 
                Arrays.asList(fixedDiscount)),
                
                new Bill(null, "customer4", Arrays.asList(
                    new BillItem(null, "Smart TV", new BigDecimal("1200.00"), 1),
                    new BillItem(null, "Sound Bar", new BigDecimal("300.00"), 1),
                    new BillItem(null, "Casual Wear Set", new BigDecimal("180.00"), 2)
                ), new BigDecimal("1860.00"), new BigDecimal("1674.00"), LocalDateTime.now(), 
                Arrays.asList(electronicsDiscount, clothingDiscount))
            );
            
            mongoTemplate.insertAll(bills);
            log.info("Inserted {} bill documents with nested items and discounts", bills.size());
        } else {
            log.info("Bills collection already exists with data");
        }
    }
}