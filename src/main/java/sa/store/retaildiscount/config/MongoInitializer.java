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
        initializeBills();
        
        log.info("MongoDB initialization completed!");
    }

    private void initializeCustomers() {
        if (mongoTemplate.count(new Query(), Customer.class) == 0) {
            log.info("Creating customers collection and inserting sample documents...");
            
            List<Customer> customers = Arrays.asList(
                new Customer("Ahmed Al-Mahmoud", "ID001", "EMPLOYEE", LocalDateTime.now().minusDays(30)),
                new Customer("Fatima Al-Zahra", "ID002", "EMPLOYEE", LocalDateTime.now().minusDays(15)),
                new Customer("Mohammed Al-Rashid", "ID003", "AFFILIATE", LocalDateTime.now().minusDays(20)),
                new Customer("Aisha Al-Nouri", "ID004", "AFFILIATE", LocalDateTime.now().minusDays(10)),
                new Customer("Omar Al-Farisi", "ID005", "REGULAR", LocalDateTime.now().minusDays(5))
            );
            
            mongoTemplate.insertAll(customers);
            log.info("Inserted {} customer documents", customers.size());
        } else {
            log.info("Customers collection already exists with data");
        }
    }


    private void initializeBills() {
        if (mongoTemplate.count(new Query(), Bill.class) == 0) {
            log.info("Creating bills collection and inserting sample documents...");
            
            // Create sample discounts for nested data
            Discount electronicsDiscount = new Discount(new BigDecimal("10"), "CUSTOMER_TYPE_DISCOUNT");
            Discount clothingDiscount = new Discount(new BigDecimal("20"), "CUSTOMER_TYPE_DISCOUNT");
            Discount fixedDiscount = new Discount(new BigDecimal("50"), "PRICE_DISCOUNT");

            Customer customer1 = new Customer("Ahmed Al-Mahmoud", "ID001", "EMPLOYEE", LocalDateTime.now().minusDays(30));
            Customer customer2 = new Customer("Fatima Al-Zahra", "ID002", "EMPLOYEE", LocalDateTime.now().minusDays(15));
            Customer customer3 = new Customer("Mohammed Al-Rashid", "ID003", "AFFILIATE", LocalDateTime.now().minusDays(20));
            Customer customer4 = new Customer("Aisha Al-Nouri", "ID004", "AFFILIATE", LocalDateTime.now().minusDays(10));

            List<Bill> bills = Arrays.asList(
                new Bill(null, customer1, Arrays.asList(
                    new BillItem("Gaming Laptop", new BigDecimal("2500.00"), 1),
                    new BillItem("Wireless Mouse", new BigDecimal("150.00"), 2)
                ), new BigDecimal("2800.00"), new BigDecimal("2520.00"), LocalDateTime.now().minusDays(3), 
                Arrays.asList(electronicsDiscount)),
                
                new Bill(null, customer2, Arrays.asList(
                    new BillItem("Designer Shirt", new BigDecimal("299.99"), 2),
                    new BillItem("Premium Jeans", new BigDecimal("199.99"), 1)
                ), new BigDecimal("799.97"), new BigDecimal("639.98"), LocalDateTime.now().minusDays(2), 
                Arrays.asList(clothingDiscount)),
                
                new Bill(null, customer3, Arrays.asList(
                    new BillItem("Coffee Maker", new BigDecimal("450.00"), 1),
                    new BillItem("Coffee Beans", new BigDecimal("75.00"), 3)
                ), new BigDecimal("675.00"), new BigDecimal("625.00"), LocalDateTime.now().minusDays(1), 
                Arrays.asList(fixedDiscount)),
                
                new Bill(null, customer4, Arrays.asList(
                    new BillItem("Smart TV", new BigDecimal("1200.00"), 1),
                    new BillItem("Sound Bar", new BigDecimal("300.00"), 1),
                    new BillItem("Casual Wear Set", new BigDecimal("180.00"), 2)
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