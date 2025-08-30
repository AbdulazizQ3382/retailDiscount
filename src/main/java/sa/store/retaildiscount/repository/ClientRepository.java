package sa.store.retaildiscount.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import sa.store.retaildiscount.entity.Client;

import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    
    Optional<Client> findByUsername(String username);
}