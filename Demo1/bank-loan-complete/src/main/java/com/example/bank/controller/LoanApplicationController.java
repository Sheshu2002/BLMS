package com.example.bank.controller;

import com.example.bank.model.LoanApplication;
import com.example.bank.model.ApprovalStatus;
import com.example.bank.model.Customer;
import com.example.bank.model.LoanProduct;
import com.example.bank.model.KycStatus;
import com.example.bank.service.LoanApplicationService;
import com.example.bank.service.CustomerService;
import com.example.bank.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/applications")
public class LoanApplicationController {
	@Autowired
	private LoanApplicationService appService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private LoanProductService productService;

	@GetMapping
	public String list(Model m) {
		m.addAttribute("applications", appService.list());
		return "applications/list";
	}

	@GetMapping("/add")
	public String addForm(Model m) {
		m.addAttribute("application", new LoanApplication());
		m.addAttribute("customers", customerService.listVerified());
		m.addAttribute("products", productService.list());
		return "applications/form";
	}

	@PostMapping("/save")
	public String save(@ModelAttribute LoanApplication application, @RequestParam Long customerId, @RequestParam Long productId, Model m) {
		Customer c = customerService.get(customerId);
		LoanProduct p = productService.get(productId).orElse(null);

		if (c == null || p == null) {
			return "redirect:/applications/add";
		}

		if (c.getKycStatus() != KycStatus.VERIFIED) {
			m.addAttribute("application", application);
			m.addAttribute("customers", customerService.listVerified());
			m.addAttribute("products", productService.list());
			m.addAttribute("selectedProductId", productId);
			m.addAttribute("error", "Selected customer's KYC is pending. Please choose a verified customer.");
			return "applications/form";
		}

		// Validate amount within product bounds
		Double amount = application.getAmount();
		if (amount == null || amount < p.getMinAmount() || amount > p.getMaxAmount()) {
			m.addAttribute("application", application);
			m.addAttribute("customers", customerService.listVerified());
			m.addAttribute("products", productService.list());
			m.addAttribute("selectedCustomerId", customerId);
			m.addAttribute("selectedProductId", productId);
			m.addAttribute("error", String.format("Amount must be between %.2f and %.2f", p.getMinAmount(), p.getMaxAmount()));
			return "applications/form";
		}

		application.setCustomer(c);
		application.setLoanProduct(p);
		application.setAppliedDate(LocalDate.now());
		application.setStatus(ApprovalStatus.PENDING);
		appService.save(application);
		return "redirect:/applications";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Long id, Model m) {
		m.addAttribute("application", appService.get(id));
		m.addAttribute("customers", customerService.listVerified());
		m.addAttribute("products", productService.list());
		return "applications/form";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable Long id) {
		appService.delete(id);
		return "redirect:/applications";
	}

	@GetMapping("/approve/{id}")
	public String approve(@PathVariable Long id) {
		LoanApplication a = appService.get(id);
		a.setStatus(ApprovalStatus.APPROVED);
		appService.save(a);
		return "redirect:/applications";
	}

	@GetMapping("/reject/{id}")
	public String reject(@PathVariable Long id) {
		LoanApplication a = appService.get(id);
		a.setStatus(ApprovalStatus.REJECTED);
		appService.save(a);
		return "redirect:/applications";
	}
}