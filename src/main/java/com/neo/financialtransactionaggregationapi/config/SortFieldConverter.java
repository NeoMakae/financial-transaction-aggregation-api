package com.neo.financialtransactionaggregationapi.config;

import com.neo.financialtransactionaggregationapi.model.SortField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortFieldConverter implements Converter<String, SortField> {

    @Override
    public SortField convert(String source) {
        for (SortField field : SortField.values()) {
            if (field.getField().equalsIgnoreCase(source)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Invalid sortBy value: " + source);
    }
}