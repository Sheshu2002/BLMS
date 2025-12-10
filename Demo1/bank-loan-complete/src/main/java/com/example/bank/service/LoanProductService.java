package com.example.bank.service;

import com.example.bank.model.LoanProduct;
import com.example.bank.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanProductService {
    @Autowired
    private LoanProductRepository repo;

    public List<LoanProduct> list() {
        return repo.findAll();
    }

    public Optional<LoanProduct> get(Long id) {
        return repo.findById(id);
    }

    public LoanProduct save(LoanProduct p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}