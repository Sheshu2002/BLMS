package com.example.bank.service;

import com.example.bank.model.LoanApplication;
import com.example.bank.model.ApprovalStatus;
import com.example.bank.repository.LoanApplicationRepository;
import com.example.bank.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {
    @Autowired
    private LoanApplicationRepository repo;
    @Autowired
    private RepaymentRepository repaymentRepo;

    public List<LoanApplication> list() { return repo.findAll(); }
    
    public List<LoanApplication> listWithRepayments() {
        return repo.findAllWithRepayments();
    }
    
    public LoanApplication get(Long id) { return repo.findById(id).orElse(null); }
    
    @Transactional
    public LoanApplication save(LoanApplication a) { return repo.save(a); }

    @Transactional
    public void delete(Long id) {
        LoanApplication app = repo.findById(id).orElse(null);
        if (app != null) {
            repaymentRepo.deleteAll(repaymentRepo.findByApplicationOrderByDueDateAsc(app));
            repo.delete(app);
        }
    }

    public List<LoanApplication> listByStatus(ApprovalStatus s) { return repo.findByStatus(s); }
}