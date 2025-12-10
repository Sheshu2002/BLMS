package com.example.bank.repository;

import com.example.bank.model.Repayment;
import com.example.bank.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByApplicationOrderByDueDateAsc(LoanApplication application);
}