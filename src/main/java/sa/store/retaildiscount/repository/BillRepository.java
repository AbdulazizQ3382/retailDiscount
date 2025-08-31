package sa.store.retaildiscount.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import sa.store.retaildiscount.entity.Bill;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends PagingAndSortingRepository<Bill, String> {
    
    List<Bill> findByCustomer_IdentityOrderByBillDateDesc(String customerId);

    Optional<Bill> findById(String billId);
}
