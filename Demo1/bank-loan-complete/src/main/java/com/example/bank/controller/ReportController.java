package com.example.bank.controller;

import com.example.bank.model.ApprovalStatus;
import com.example.bank.service.LoanApplicationService;
import com.example.bank.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.transaction.Transactional; // Import the Transactional annotation

@Controller
@RequiredArgsConstructor
public class ReportController {
    @Autowired
    LoanApplicationService appService;
    @Autowired
    RepaymentService repayService;

    @GetMapping("/reports")
    @Transactional // Add this annotation to enable lazy loading
    public String reports(Model m) {
        m.addAttribute("totalApplications", appService.list().size());
        m.addAttribute("approved", appService.listByStatus(ApprovalStatus.APPROVED).size());
        m.addAttribute("pending", appService.listByStatus(ApprovalStatus.PENDING).size());

        // Use service.outstanding to include interest on remaining principal
        double outstandingTotal = appService.list().stream()
                .filter(a -> a.getStatus() == ApprovalStatus.APPROVED)
                .map(a -> repayService.outstanding(a.getRepayments()))
                .mapToDouble(Double::doubleValue)
                .sum();
        m.addAttribute("outstandingTotal", outstandingTotal);

        return "reports/index";
    }
}