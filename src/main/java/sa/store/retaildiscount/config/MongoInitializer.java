package sa.store.retaildiscount.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
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
                    LocalDateTime.now(), LocalDateTime.now().plusDays(30), true, "Electronics"),
                new Discount(null, "CLOTHING20", "20% discount on clothing items", new BigDecimal("20"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(15), true, "Clothing"),
                new Discount(null, "APPLIANCES15", "15% discount on home appliances", new BigDecimal("15"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(45), true, "Appliances"),
                new Discount(null, "NEWUSER50", "50 SAR off for new customers", null, new BigDecimal("50"),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(60), true, null),
                new Discount(null, "SUMMER25", "25% summer sale discount", new BigDecimal("25"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(90), true, null),
                new Discount(null, "BOOKS10", "10% discount on books", new BigDecimal("10"), null,
                    LocalDateTime.now(), LocalDateTime.now().plusDays(20), true, "Books")
            );
            
            mongoTemplate.insertAll(discounts);
            log.info("Inserted {} discount documents", discounts.size());
        } else {
            log.info("Discounts collection already exists with data");
        }
    }
}