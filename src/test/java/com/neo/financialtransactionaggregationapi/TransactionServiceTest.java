package com.neo.financialtransactionaggregationapi;

import com.neo.financialtransactionaggregationapi.exception.BadRequestException;
import com.neo.financialtransactionaggregationapi.exception.ResourceNotFoundException;
import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import com.neo.financialtransactionaggregationapi.service.DataLoaderService;
import com.neo.financialtransactionaggregationapi.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private DataLoaderService dataLoaderService;

    @InjectMocks
    private TransactionService service;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transaction1 = Transaction.builder().id(1L).customerId("cust1").description("Groceries").amount(BigDecimal.valueOf(100)).category(Category.FOOD).timestamp(LocalDateTime.now()).build();

        transaction2 = Transaction.builder().id(2L).customerId("cust1").description("Uber ride").amount(BigDecimal.valueOf(50)).category(Category.TRAVEL).timestamp(LocalDateTime.now()).build();
    }

    @Test
    void testSave_WithCategory() {
        when(repository.save(transaction1)).thenReturn(transaction1);

        Transaction saved = service.save(transaction1);

        verify(repository).save(transaction1);
        assertEquals(transaction1, saved);
    }

    @Test
    void testSave_WithoutCategory_ShouldAutoCategorize() {
        Transaction uncategorized = Transaction.builder().id(3L).description("Movie").amount(BigDecimal.valueOf(80)).timestamp(LocalDateTime.now()).build();

        when(dataLoaderService.categorize("Movie")).thenReturn(Category.ENTERTAINMENT);
        when(repository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction saved = service.save(uncategorized);

        assertEquals(Category.ENTERTAINMENT, saved.getCategory());
        verify(repository).save(saved);
        verify(dataLoaderService).categorize("Movie");
    }

    @Test
    void testGetTransactionById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(transaction1));

        Transaction found = service.getTransactionById(1L);

        assertEquals(transaction1, found);
    }

    @Test
    void testGetTransactionById_NotFound() {
        when(repository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getTransactionById(10L));
    }

    @Test
    void testGetFilteredTransactions_ByCategory() {
        when(repository.findAll()).thenReturn(List.of(transaction1, transaction2));

        List<Transaction> filtered = service.getFilteredTransactions(null, Category.FOOD, null, null);

        assertEquals(1, filtered.size());
        assertEquals(Category.FOOD, filtered.get(0).getCategory());
    }

    @Test
    void testAggregateByCategory() {
        when(repository.findAll()).thenReturn(List.of(transaction1, transaction2));

        Map<Category, BigDecimal> aggregated = service.aggregateByCategory(null, null, null, null);

        assertEquals(2, aggregated.size());
        assertEquals(BigDecimal.valueOf(100), aggregated.get(Category.FOOD));
        assertEquals(BigDecimal.valueOf(50), aggregated.get(Category.TRAVEL));
    }

    @Test
    void testAggregateByDate() {
        LocalDate today = LocalDate.now();
        when(repository.findAll()).thenReturn(List.of(transaction1, transaction2));

        Map<Category, BigDecimal> aggregated = service.aggregateByDate(null, null, today.minusDays(1), today.plusDays(1));

        assertEquals(2, aggregated.size());
        assertEquals(BigDecimal.valueOf(100), aggregated.get(Category.FOOD));
        assertEquals(BigDecimal.valueOf(50), aggregated.get(Category.TRAVEL));
    }

    @Test
    void testGetTotal() {
        when(repository.findAll()).thenReturn(List.of(transaction1, transaction2));

        BigDecimal total = service.getTotal(null, null, null, null);

        assertEquals(BigDecimal.valueOf(150), total);
    }

    @Test
    void testValidateDateRange_Valid() {
        assertDoesNotThrow(() -> service.validateDateRange(LocalDate.now().minusDays(1), LocalDate.now()));
    }

    @Test
    void testValidateDateRange_Invalid() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().minusDays(1);

        assertThrows(BadRequestException.class, () -> service.validateDateRange(start, end));
    }
}