package com.neo.financialtransactionaggregationapi.service;

import com.neo.financialtransactionaggregationapi.exception.BadRequestException;
import com.neo.financialtransactionaggregationapi.exception.ResourceNotFoundException;
import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "filteredTransactions", key = "#customerId + '-' + #category + '-' + #start + '-' + #end")
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

    @Cacheable(value = "aggregateByDate", key = "#customerId + '-' + #category + '-' + #start + '-' + #end")
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
                .filter(t -> category == null || t.getCategory() == category)
                .filter(t -> {
                    LocalDate date = t.getTimestamp().toLocalDate();
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : Category.UNCATEGORIZED,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    @Cacheable(value = "aggregateByCategory", key = "#customerId + '-' + #category + '-' + #start + '-' + #end")
    public Map<Category, BigDecimal> aggregateByCategory(
            String customerId,
            Category category,
            LocalDate start,
            LocalDate end
    ) {
        List<Transaction> transactions = (customerId != null)
                ? repository.findByCustomerId(customerId)
                : repository.findAll();

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

    @CacheEvict(value = {"transactions", "filteredTransactions", "aggregateByCategory", "aggregateByDate"}, allEntries = true)
    public Transaction save(Transaction transaction) {
        if(transaction.getCategory() != null) return repository.save(transaction);
        transaction.setCategory(dataLoaderService.categorize(transaction.getDescription()));
        return repository.save(transaction);
    }

    @Cacheable(value = "transactions", key = "#id")
    public Transaction getTransactionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private List<Transaction> getTransactions(String customerId) {
        return customerId != null
                ? repository.findByCustomerId(customerId)
                : repository.findAll();
    }

    public void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }
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
