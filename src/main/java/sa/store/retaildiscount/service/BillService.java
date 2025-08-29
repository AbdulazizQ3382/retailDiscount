package sa.store.retaildiscount.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

        Bill bill = billRepository.findById(billId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found with ID: " + billId));

            return billMapper.billEntityToBillDTO(bill);
    }

    public List<BillDTO> getBillsByCustomerId(String customerIdentity) {

        List<Bill> bills = billRepository.findByCustomer_Identity(customerIdentity);

        if(bills.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No bills found for customer ID: " + customerIdentity);
        }

        return bills.stream()
                .map(billMapper::billEntityToBillDTO)
                .collect(Collectors.toList());
    }

}