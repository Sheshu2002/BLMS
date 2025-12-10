package com.example.bank.service;

import com.example.bank.model.LoanApplication;
import com.example.bank.model.Repayment;
import com.example.bank.model.ApprovalStatus;
import com.example.bank.repository.RepaymentRepository;
import com.example.bank.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RepaymentService {
    @Autowired
    private RepaymentRepository repayRepo ;
    @Autowired
    private LoanApplicationRepository appRepo;

    @Transactional
    public List<Repayment> generateSchedule(Long applicationId) {
        LoanApplication app = appRepo.findById(applicationId).orElseThrow();
        if (app.getStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Schedule can only be generated for approved applications");
        }
        if (app.getLoanProduct() == null || app.getLoanProduct().getTenureMonths() == null || app.getLoanProduct().getTenureMonths() <= 0) {
            throw new IllegalArgumentException("Invalid loan tenure for this application");
        }
        if (app.getAmount() == null || app.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid loan amount for this application");
        }
        // Return existing schedule if already generated
        List<Repayment> existing = repayRepo.findByApplicationOrderByDueDateAsc(app);
        if (!existing.isEmpty()) {
            return existing;
        }

        // EMI schedule based on interest-bearing amortized payments
        double principal = app.getAmount();
        int months = app.getLoanProduct().getTenureMonths();
        double annualInterestRate = app.getLoanProduct().getInterestRate();
        double monthRate = annualInterestRate / (12 * 100.0);
        double emi;
        if (monthRate == 0.0) {
            emi = principal / months;
        } else {
            emi = (principal * monthRate * Math.pow(1 + monthRate, months)) / (Math.pow(1 + monthRate, months) - 1);
        }
        emi = Math.floor(emi);

        List<Repayment> list = new ArrayList<>();
        for (int i = 1; i <= months; i++) {
            Repayment r = new Repayment();
            r.setApplication(app);
            r.setDueDate(LocalDate.now().plusMonths(i));
            r.setAmountDue(emi);
            r.setCompleted(false);
            list.add(r);
        }
        return repayRepo.saveAll(list);
    }

    public List<Repayment> scheduleFor(Long applicationId) {
        LoanApplication app = appRepo.findById(applicationId).orElseThrow();
        return repayRepo.findByApplicationOrderByDueDateAsc(app);
    }

    @Transactional
    public List<Repayment> ensureSchedule(Long applicationId) {
        List<Repayment> current = scheduleFor(applicationId);
        if (current.isEmpty()) {
            return generateSchedule(applicationId);
        }
        return current;
    }

    public Repayment pay(Long repaymentId) {
        Repayment r = repayRepo.findById(repaymentId).orElseThrow();
        r.setCompleted(true);
        r.setPaymentDate(LocalDate.now());
        return repayRepo.save(r);
    }

    public Repayment unpay(Long repaymentId) {
        Repayment r = repayRepo.findById(repaymentId).orElseThrow();
        r.setCompleted(false);
        r.setPaymentDate(null);
        return repayRepo.save(r);
    }

    // Outstanding is EMI times remaining unpaid installments; falls back to sum if no interest
    public double outstanding(List<Repayment> repayments) {
        if (repayments == null || repayments.isEmpty()) {
            return 0.0d;
        }
        // Determine unpaid count and associated application
        List<Repayment> unpaid = repayments.stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getCompleted() == null || !r.getCompleted())
                .sorted(Comparator.comparing(Repayment::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        if (unpaid.isEmpty()) {
            return 0.0d;
        }
        LoanApplication app = unpaid.get(0).getApplication();
        if (app == null || app.getLoanProduct() == null || app.getLoanProduct().getInterestRate() == null) {
            return unpaid.stream()
                    .map(Repayment::getAmountDue)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
        double principal = app.getAmount() == null ? 0.0 : app.getAmount();
        int months = app.getLoanProduct().getTenureMonths() == null ? unpaid.size() : app.getLoanProduct().getTenureMonths();
        double annualInterestRate = app.getLoanProduct().getInterestRate();
        double monthRate = annualInterestRate / (12 * 100.0);
        double emi;
        if (monthRate == 0.0 || principal <= 0 || months <= 0) {
            // Fallback to summing unpaid when malformed
            return unpaid.stream()
                    .map(Repayment::getAmountDue)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .sum();
        } else {
            emi = (principal * monthRate * Math.pow(1 + monthRate, months)) / (Math.pow(1 + monthRate, months) - 1);
            emi = Math.floor(emi);
            int remaining = unpaid.size();
            double total = emi * remaining;
            return Math.round(total * 100.0) / 100.0;
        }
    }
}