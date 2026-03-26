package com.neo.financialtransactionaggregationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FinancialTransactionAggregationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialTransactionAggregationApiApplication.class, args);
    }

}
