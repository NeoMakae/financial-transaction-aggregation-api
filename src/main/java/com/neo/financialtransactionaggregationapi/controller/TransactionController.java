package com.neo.financialtransactionaggregationapi.controller;

import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionRepository repository;

    @GetMapping
    public List<Transaction> getAllTransactions(@RequestParam(required = false) String customerId) {
        if(customerId != null) {
            return repository.findByCustomerId(customerId);
        }
        return repository.findAll();
    }
}