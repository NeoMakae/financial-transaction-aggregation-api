package com.neo.financialtransactionaggregationapi.service;

import com.neo.financialtransactionaggregationapi.model.Category;
import com.neo.financialtransactionaggregationapi.model.Transaction;
import com.neo.financialtransactionaggregationapi.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final TransactionRepository repository;

    private final Map<String, Category> merchantCategoryMap = new HashMap<>();

    private static final Pattern FOOD_PATTERN = Pattern.compile("\\b(food|groceries|supermarket|restaurant)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAVEL_PATTERN = Pattern.compile("\\b(taxi|ride|bus|train|travel)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENTERTAINMENT_PATTERN = Pattern.compile("\\b(tv|movie|show|entertainment|music)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BILLS_PATTERN = Pattern.compile("\\b(bill|electricity|water|municipal|internet)\\b", Pattern.CASE_INSENSITIVE);


    @PostConstruct
    public void loadData() throws Exception {
        loadCategoryConfig();
        loadJson();
        loadCsv();
    }

    private void loadJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/transactions.json");
        if (is == null) return;

        List<TransactionJson> transactions = mapper.readValue(is, new TypeReference<List<TransactionJson>>() {});
        for (TransactionJson t : transactions) {
            boolean exists = repository.existsByCustomerIdAndDescriptionAndAmountAndTimestamp(
                    t.getCustomerId(),
                    t.getDescription(),
                    t.getAmount(),
                    LocalDateTime.parse(t.getTimestamp())
            );

            if (!exists) {
                repository.save(Transaction.builder()
                        .customerId(t.getCustomerId())
                        .description(t.getDescription())
                        .amount(t.getAmount())
                        .category(categorize(t.getDescription()))
                        .timestamp(LocalDateTime.parse(t.getTimestamp()))
                        .build());
            }
        }
    }

    private void loadCsv() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(getClass().getClassLoader()
                .getResource("data/transactions.csv").toURI()));
        lines.remove(0); // remove header

        for (String line : lines) {
            String[] parts = line.split(",");
            boolean exists = repository.existsByCustomerIdAndDescriptionAndAmountAndTimestamp(
                    parts[0],
                    parts[1],
                    new BigDecimal(parts[2]),
                    LocalDateTime.parse(parts[3])
            );

            if (!exists) {
                repository.save(Transaction.builder()
                        .customerId(parts[0])
                        .description(parts[1])
                        .amount(new BigDecimal(parts[2]))
                        .category(categorize(parts[1]))
                        .timestamp(LocalDateTime.parse(parts[3]))
                        .build());
            }
        }
    }

    private void loadCategoryConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getClassLoader().getResourceAsStream("categories.json");
        if (is == null) return;

        Map<String, List<String>> categories = mapper.readValue(is, new TypeReference<Map<String, List<String>>>() {});

        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            Category cat = Category.valueOf(entry.getKey());
            for (String merchant : entry.getValue()) {
                merchantCategoryMap.put(merchant.toLowerCase(), cat);
            }
        }
    }

    public Category categorize(String description) {
        String desc = description.toLowerCase();

        for (Map.Entry<String, Category> entry : merchantCategoryMap.entrySet()) {
            if (desc.contains(entry.getKey())) return entry.getValue();
        }

        if (FOOD_PATTERN.matcher(desc).find()) return Category.FOOD;
        if (TRAVEL_PATTERN.matcher(desc).find()) return Category.TRAVEL;
        if (ENTERTAINMENT_PATTERN.matcher(desc).find()) return Category.ENTERTAINMENT;
        if (BILLS_PATTERN.matcher(desc).find()) return Category.BILLS;

        return Category.UNCATEGORIZED;
    }



    private static final Map<String, Category> MERCHANT_CATEGORY_MAP = Map.ofEntries(
            Map.entry("woolworths", Category.FOOD),
            Map.entry("checkers", Category.FOOD),
            Map.entry("pick n pay", Category.FOOD),
            Map.entry("spar", Category.FOOD),
            Map.entry("shoprite", Category.FOOD),

            Map.entry("uber", Category.TRAVEL),
            Map.entry("bolt", Category.TRAVEL),
            Map.entry("taxi", Category.TRAVEL),
            Map.entry("lyft", Category.TRAVEL),

            Map.entry("dstv", Category.ENTERTAINMENT),
            Map.entry("showmax", Category.ENTERTAINMENT),
            Map.entry("netflix", Category.ENTERTAINMENT),
            Map.entry("music", Category.ENTERTAINMENT),

            Map.entry("eskom", Category.BILLS),
            Map.entry("municipal", Category.BILLS),
            Map.entry("water", Category.BILLS),
            Map.entry("electricity", Category.BILLS)
    );

    @Setter
    @Getter
    private static class TransactionJson {
        private String customerId;
        private String description;
        private BigDecimal amount;
        private String timestamp;

    }
}