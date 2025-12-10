package com.example.bank.controller;

import com.example.bank.model.LoanProduct;
import com.example.bank.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class LoanProductController {
    @Autowired
    private LoanProductService service;

    @GetMapping
    public String list(Model m) {
        m.addAttribute("products", service.list());
        return "products/list";
    }

    @GetMapping("/add")
    public String add(Model m) {
        m.addAttribute("product", new LoanProduct());
        return "products/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LoanProduct p, Model m) {
        if (p.getMinAmount() != null && p.getMaxAmount() != null && p.getMaxAmount() <= p.getMinAmount()) {
            m.addAttribute("product", p);
            m.addAttribute("errorMessage", "Maximum amount must be greater than minimum amount.");
            return "products/form";
        }
        service.save(p);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model m) {
        Optional<LoanProduct> product = service.get(id);
        if (product.isPresent()) {
            m.addAttribute("product", product.get());
            return "products/form";
        }
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/products";
    }
}