package com.neo.financialtransactionaggregationapi.controller;

import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Operation(
            summary = "Get all transactions",
            description = "Fetch all transactions, optionally filtered by customerId, category, or date range"
    )@GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("GET /transactions called with customerId={}, category={}, start={}, end={}", customerId, category, start, end);
        service.validateDateRange(start, end);
        List<Transaction> transactions = service.getFilteredTransactions(customerId, category, start, end);
        log.info("Returning {} transactions", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieve a single transaction by its unique ID"
    )
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.info("GET /transactions/{} called", id);
        Transaction transaction = service.getTransactionById(id);
        log.info("Transaction {} retrieved successfully", id);
        return ResponseEntity.ok(transaction);
    }

    @Operation(
            summary = "Create a new transaction",
            description = "Add a new transaction record. The transaction data must be valid."
    )
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        log.info("POST /transactions called with transaction: description={}, amount={}", transaction.getDescription(), transaction.getAmount());
        Transaction saved = service.save(transaction);
        log.info("Transaction {} saved successfully", saved.getId());
        return ResponseEntity.status(201).body(saved);
    }

    @Operation(
            summary = "Aggregate transactions by category",
            description = "Return a map of total transaction amounts grouped by category. Optionally filter by customerId, category, or date range."
    )
    @GetMapping("/transactions/aggregate")
    public ResponseEntity<Map<Category, BigDecimal>> aggregateByCategory(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("GET /transactions/aggregate called with customerId={}, category={}, start={}, end={}", customerId, category, start, end);
        service.validateDateRange(start, end);
        Map<Category, BigDecimal> result = service.aggregateByCategory(customerId, category, start, end);
        log.info("Returning aggregated results for {} categories", result.size());
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get total transaction amount",
            description = "Calculate the total sum of transactions. Optionally filter by customerId, category, or date range."
    )
    @GetMapping("/transactions/aggregate/total")
    public ResponseEntity<BigDecimal> getTotal(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("GET /transactions/aggregate/toatl called with customerId={}, category={}, start={}, end={}", customerId, category, start, end);
        service.validateDateRange(start, end);
        BigDecimal total = service.getTotal(customerId, category, start, end);
        log.info("Returning aggregated results total for {} all categories", total);
        return ResponseEntity.ok(total);
    }

    @Operation(
            summary = "Get aggregated spending by date",
            description = "Return a map of spending totals grouped by category over a specific date range. Start and end dates are required."
    )
    @GetMapping("/transactions/aggregate/spending")
    public ResponseEntity<Map<Category, BigDecimal>> getAggregatedSpending(@RequestParam(required = false) String customerId, @RequestParam(required = false) Category category, @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("GET /transactions/aggregate/spending called with customerId={}, category={}, start={}, end={}", customerId, category, start, end);
        service.validateDateRange(start, end);
        Map<Category, BigDecimal> aggregatedSpending = service.aggregateByDate(customerId, category, start, end);
        log.info("Aggregated spending calculated for {} categories", aggregatedSpending.size());
        return ResponseEntity.ok(aggregatedSpending);
    }
}