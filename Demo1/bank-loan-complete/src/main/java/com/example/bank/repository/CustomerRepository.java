package com.example.bank.repository;

import com.example.bank.model.Customer;
import com.example.bank.model.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>
{
    Optional<Customer> findByEmail(String email);
    List<Customer> findByKycStatus(KycStatus status);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}