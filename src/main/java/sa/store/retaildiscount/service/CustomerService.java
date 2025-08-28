package sa.store.retaildiscount.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import sa.store.retaildiscount.entity.Customer;

@Service
public class CustomerService {


    private final MongoTemplate mongoTemplate;

    @Autowired
    public CustomerService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }




    public Customer getCustomerById(String customerId) {
        return this.mongoTemplate.findById(customerId, Customer.class);
    }
}
