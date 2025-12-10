package com.example.bank.controller;

import com.example.bank.model.Customer;
import com.example.bank.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    CustomerService service;

    @GetMapping
    public String list(Model m) {
        m.addAttribute("customers", service.list());
        return "customers/list";
    }

    @GetMapping("/add")
    public String add(Model m) {
        m.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Customer customer, Model m) {
        try {
            service.save(customer);
            return "redirect:/customers";
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            m.addAttribute("customer", customer);
            m.addAttribute("errorMessage", msg);
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("email")) {
                    m.addAttribute("emailError", msg);
                }
                if (lower.contains("name")) {
                    m.addAttribute("nameError", msg);
                }
            }
            return "customers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model m) {
        m.addAttribute("customer", service.get(id));
        return "customers/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/customers";
    }
}