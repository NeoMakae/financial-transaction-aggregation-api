package com.neo.financialtransactionaggregationapi.repository;

import com.neo.financialtransactionaggregationapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerId(String customerId);
}