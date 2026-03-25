package com.neo.financialtransactionaggregationapi.controller;

import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        service.validateDateRange(start, end);
        return ResponseEntity.ok(service.getFilteredTransactions(customerId, category, start, end));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTransactionById(id));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.status(201).body(service.save(transaction));
    }

    @GetMapping("/transactions/aggregate")
    public ResponseEntity<Map<Category, BigDecimal>> aggregateByCategory(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        service.validateDateRange(start, end);
        return ResponseEntity.ok(service.aggregateByCategory(customerId, category, start, end));
    }

    @GetMapping("/transactions/aggregate/total")
    public ResponseEntity<BigDecimal> getTotal(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        service.validateDateRange(start, end);
        return ResponseEntity.ok(service.getTotal(customerId, category, start, end));
    }

    @GetMapping("/transactions/aggregate/spending")
    public ResponseEntity<Map<Category, BigDecimal>> getAggregatedSpending(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        service.validateDateRange(start, end);
        return ResponseEntity.ok(service.aggregateByDate(customerId, category, start, end));
    }
}