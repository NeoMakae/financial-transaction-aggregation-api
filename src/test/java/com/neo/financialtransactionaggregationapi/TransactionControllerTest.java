package com.neo.financialtransactionaggregationapi;

import com.neo.financialtransactionaggregationapi.controller.TransactionController;
import com.neo.financialtransactionaggregationapi.exception.ResourceNotFoundException;
import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    @Mock
    private TransactionService service;

    @InjectMocks
    private TransactionController controller;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setCustomerId("cust1");
        transaction1.setCategory(Category.FOOD);
        transaction1.setAmount(new BigDecimal("100"));

        transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setCustomerId("cust1");
        transaction2.setCategory(Category.ENTERTAINMENT);
        transaction2.setAmount(new BigDecimal("200"));
    }

    @Test
    void testGetTransactions_noFilters() {
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(service.getFilteredTransactions(null, null, null, null)).thenReturn(transactions);

        ResponseEntity<List<Transaction>> response = controller.getTransactions(null, null, null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(service).getFilteredTransactions(null, null, null, null);
    }

    @Test
    void testGetTransactionById_found() {
        when(service.getTransactionById(1L)).thenReturn(transaction1);

        ResponseEntity<Transaction> response = controller.getTransaction(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Category.FOOD, response.getBody().getCategory());
        verify(service).getTransactionById(1L);
    }

    @Test
    void testGetTransactionById_notFound() {
        when(service.getTransactionById(99L))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        assertThrows(ResourceNotFoundException.class, () -> controller.getTransaction(99L));

        verify(service).getTransactionById(99L);
    }

    @Test
    void testCreateTransaction() {
        when(service.save(any(Transaction.class))).thenReturn(transaction1);

        ResponseEntity<Transaction> response = controller.createTransaction(transaction1);

        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("cust1", response.getBody().getCustomerId());
        verify(service).save(transaction1);
    }

    @Test
    void testAggregateByCategory() {
        Map<Category, BigDecimal> aggregate = Map.of(
                Category.FOOD, new BigDecimal("100"),
                Category.ENTERTAINMENT, new BigDecimal("200")
        );

        when(service.aggregateByCategory("cust1", Category.FOOD, null, null)).thenReturn(aggregate);

        ResponseEntity<Map<Category, BigDecimal>> response = controller.aggregateByCategory("cust1", Category.FOOD, null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(service).aggregateByCategory("cust1", Category.FOOD, null, null);
    }

    @Test
    void testGetTotal() {
        BigDecimal total = new BigDecimal("300");

        when(service.getTotal("cust1", null, null, null)).thenReturn(total);

        ResponseEntity<BigDecimal> response = controller.getTotal("cust1", null, null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(total, response.getBody());
        verify(service).getTotal("cust1", null, null, null);
    }

    @Test
    void testGetAggregatedSpending() {
        Map<Category, BigDecimal> spending = Map.of(
                Category.FOOD, new BigDecimal("150"),
                Category.ENTERTAINMENT, new BigDecimal("50")
        );

        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        when(service.aggregateByDate("cust1", null, start, end)).thenReturn(spending);

        ResponseEntity<Map<Category, BigDecimal>> response = controller.getAggregatedSpending("cust1", null, start, end);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(service).aggregateByDate("cust1", null, start, end);
    }
}