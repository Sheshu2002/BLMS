package com.example.bank.controller;

import com.example.bank.model.Repayment;
import com.example.bank.service.RepaymentService;
import com.example.bank.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/repayments")
public class RepaymentController {
    @Autowired
    private RepaymentService service;
    @Autowired
    private LoanApplicationService appService;

    @GetMapping
    public String list(Model m) {
        m.addAttribute("applications", appService.listWithRepayments());
        return "repayments/list";
    }

    @GetMapping("/generate/{appId}")
    public String generate(@PathVariable Long appId, RedirectAttributes redirectAttributes) {
        try {
            service.generateSchedule(appId);
            redirectAttributes.addFlashAttribute("successMessage", "Repayment schedule generated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to generate schedule. Please check the application.");
        }
        return "redirect:/repayments";
    }

    @GetMapping("/view/{appId}")
    public String view(@PathVariable Long appId, Model m, RedirectAttributes redirectAttributes) {
        try {
            List<Repayment> schedules = service.scheduleFor(appId);
            if (schedules.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "No repayment schedule found for this loan. Please generate it first.");
                return "redirect:/repayments";
            }
            m.addAttribute("schedules", schedules);
            m.addAttribute("application", appService.get(appId));
            m.addAttribute("outstanding", service.outstanding(schedules));
            return "repayments/view";
        } catch (Exception e) {
            e.printStackTrace(); // Add this line to print the error in the console
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to view schedule for this application.");
            return "redirect:/repayments";
        }
    }

    @GetMapping("/pay/{repaymentId}")
    public String pay(@PathVariable Long repaymentId, RedirectAttributes redirectAttributes) {
        try {
            Repayment r = service.pay(repaymentId);
            redirectAttributes.addFlashAttribute("successMessage", "Repayment marked as paid.");
            Long appId = r.getApplication() != null ? r.getApplication().getId() : null;
            return appId != null ? "redirect:/repayments/view/" + appId : "redirect:/repayments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark repayment as paid.");
            return "redirect:/repayments";
        }
    }

    @GetMapping("/undo/{repaymentId}")
    public String undo(@PathVariable Long repaymentId, RedirectAttributes redirectAttributes) {
        try {
            Repayment r = service.unpay(repaymentId);
            redirectAttributes.addFlashAttribute("successMessage", "Payment undone.");
            Long appId = r.getApplication() != null ? r.getApplication().getId() : null;
            return appId != null ? "redirect:/repayments/view/" + appId : "redirect:/repayments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to undo payment.");
            return "redirect:/repayments";
        }
    }
}