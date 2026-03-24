package com.neo.financialtransactionaggregationapi.controller;

import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import com.neo.financialtransactionaggregationapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    private final TransactionRepository repository;

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.getFilteredTransactions(customerId, category, start, end);
    }

    @GetMapping("/transactions/{id}")
    public Optional<Transaction> getTransaction(@PathVariable Long id) {
        return repository.findById(id);
    }

    @PostMapping("/transactions")
    public Transaction createTransaction(@Valid @RequestBody Transaction transaction) {
        return service.save(transaction);
    }

    @GetMapping("/transactions/aggregate")
    public Map<Category, BigDecimal> aggregateByCategory(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = true) Category category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.aggregateByCategory(customerId, category, start, end);
    }

    @GetMapping("/transactions/aggregate/total")
    public BigDecimal getTotal(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        return service.getTotal(customerId, category, start, end);
    }

    //Get aggregated spending on a date range
    @GetMapping("transactions/aggregate/spending")
    public Map<Category, BigDecimal> getAggregatedSpending(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Category category,
            @RequestParam(required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return service.aggregateByDate(customerId, category, start, end);
    }
}