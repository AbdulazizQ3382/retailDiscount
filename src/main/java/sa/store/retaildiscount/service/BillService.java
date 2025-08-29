package sa.store.retaildiscount.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sa.store.retaildiscount.dto.BillDTO;
import sa.store.retaildiscount.entity.Bill;
import sa.store.retaildiscount.mapper.BillMapper;
import sa.store.retaildiscount.repository.BillRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;

    @Autowired
    public BillService(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    public BillDTO getBillById(String billId) {
        Optional<Bill> bill = billRepository.findById(billId);
        if (bill.isPresent()) {
            return billMapper.billEntityToBillDTO(bill.get());
        } else {
            throw new RuntimeException("Bill not found with ID: " + billId);
        }
    }

    public List<BillDTO> getBillsByCustomerId(String customerIdentity) {
        List<Bill> bills = billRepository.findByCustomer_Identity(customerIdentity);
        return bills.stream()
                .map(billMapper::billEntityToBillDTO)
                .collect(Collectors.toList());
    }

}