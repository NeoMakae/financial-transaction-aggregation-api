package com.neo.financialtransactionaggregationapi.service;

import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    private final DataLoaderService dataLoaderService;

    public List<Transaction> getTransactions(String customerId, LocalDate start, LocalDate end) {
        List<Transaction> transactions = (customerId != null)
                ? repository.findByCustomerId(customerId)
                : repository.findAll();

        return transactions.stream()
                .filter(t -> {
                    LocalDate date = t.getTimestamp().toLocalDate();
                    boolean afterStart = (start == null || !date.isBefore(start));
                    boolean beforeEnd = (end == null || !date.isAfter(end));
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }

    public List<Transaction> getFilteredTransactions(
            String customerId,
            Category category,
            LocalDate start,
            LocalDate end
    ) {
        return getTransactions(customerId).stream()
                .filter(byCategory(category))
                .filter(byStartDate(start))
                .filter(byEndDate(end))
                .toList();
    }

    public Map<Category, BigDecimal> aggregateByDate(
            String customerId,
            Category category,
            LocalDate start,
            LocalDate end
    ) {
        List<Transaction> transactions = (customerId != null)
                ? repository.findByCustomerId(customerId)
                : repository.findAll();

        return transactions.stream()
                // filter by category if provided
                .filter(t -> category == null || t.getCategory() == category)
                // filter by mandatory date range
                .filter(t -> {
                    LocalDate date = t.getTimestamp().toLocalDate();
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                // aggregate by category safely (map nulls to UNCATEGORIZED)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : Category.UNCATEGORIZED,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    public Map<Category, BigDecimal> aggregateByCategory(
            String customerId,
            Category category,
            LocalDate start,
            LocalDate end
    ) {
        List<Transaction> transactions;
        if (customerId != null) {
            transactions = repository.findByCustomerId(customerId);
        } else {
            transactions = repository.findAll();
        }
        return transactions.stream()
                .filter(t -> category == null || t.getCategory() == category)
                .filter(t -> start == null || !t.getTimestamp().toLocalDate().isBefore(start))
                .filter(t -> end == null || !t.getTimestamp().toLocalDate().isAfter(end))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    public BigDecimal getTotal(
            String customerId,
            Category category,
            LocalDate start,
            LocalDate end
    ) {
        return getFilteredTransactions(customerId, category, start, end)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Transaction save(Transaction transaction) {
        if(transaction.getCategory() != null) return repository.save(transaction);
        transaction.setCategory(dataLoaderService.categorize(transaction.getDescription()));
        return repository.save(transaction);
    }

    public Transaction getTransaction(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    private List<Transaction> getTransactions(String customerId) {
        return customerId != null
                ? repository.findByCustomerId(customerId)
                : repository.findAll();
    }

    private Predicate<Transaction> byCategory(Category category) {
        return t -> category == null || t.getCategory() == category;
    }

    private Predicate<Transaction> byStartDate(LocalDate start) {
        return t -> start == null || !t.getTimestamp().toLocalDate().isBefore(start);
    }

    private Predicate<Transaction> byEndDate(LocalDate end) {
        return t -> end == null || !t.getTimestamp().toLocalDate().isAfter(end);
    }
}
