package com.neo.financialtransactionaggregationapi.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortField {
    ID("id"),
    CUSTOMER_ID("customerId"),
    DESCRIPTION("description"),
    AMOUNT("amount"),
    CATEGORY("category"),
    TIMESTAMP("timestamp");

    private final String field;

    SortField(String field) {
        this.field = field;
    }

    @JsonValue
    public String getField() {
        return field;
    }
}
