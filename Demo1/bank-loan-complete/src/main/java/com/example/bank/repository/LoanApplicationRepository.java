package com.example.bank.repository;

import com.example.bank.model.LoanApplication;
import com.example.bank.model.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByStatus(ApprovalStatus status);
    
    @Query("SELECT DISTINCT app FROM LoanApplication app LEFT JOIN FETCH app.repayments")
    List<LoanApplication> findAllWithRepayments();

    // The method to count applications for a specific customer with a status that is not rejected
    long countByCustomerIdAndStatusNot(Long customerId, ApprovalStatus status);
}