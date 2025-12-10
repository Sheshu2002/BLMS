package com.example.bank.service;

import com.example.bank.model.Customer;
import com.example.bank.model.ApprovalStatus;
import com.example.bank.model.KycStatus;
import com.example.bank.repository.CustomerRepository;
import com.example.bank.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository repo;
    @Autowired
    LoanApplicationRepository loanApplicationRepo;

    public List<Customer> list() { return repo.findAll(); }
    public List<Customer> listVerified() { return repo.findByKycStatus(KycStatus.VERIFIED); }
    public Customer get(Long id) { return repo.findById(id).orElse(null); }
    public Customer save(Customer c) {
        validateDuplicates(c);
        return repo.save(c);
    }

    private void validateDuplicates(Customer c) {
        Long id = c.getId();
        String email = c.getEmail() != null ? c.getEmail().trim() : null;
        String name = c.getName() != null ? c.getName().trim() : null;
        if (email != null && !email.isEmpty()) {
            boolean exists = (id == null)
                    ? repo.existsByEmailIgnoreCase(email)
                    : repo.existsByEmailIgnoreCaseAndIdNot(email, id);
            if (exists) {
                throw new IllegalArgumentException("email already exists");
            }
        }
        if (name != null && !name.isEmpty()) {
            boolean exists = (id == null)
                    ? repo.existsByNameIgnoreCase(name)
                    : repo.existsByNameIgnoreCaseAndIdNot(name, id);
            if (exists) {
                throw new IllegalArgumentException("name already exists");
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        if (loanApplicationRepo.countByCustomerIdAndStatusNot(id, ApprovalStatus.REJECTED) > 0) {
            throw new RuntimeException("Cannot delete customer with pending or approved loan applications.");
        }
        repo.deleteById(id);
    }
}