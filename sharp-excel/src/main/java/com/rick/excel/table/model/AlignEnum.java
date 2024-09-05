package com.rick.excel.table.model;

import com.fasterxml.jackson.annotation.JsonValue;


public enum AlignEnum {
    LEFT, CENTER, RIGHT;

    @JsonValue
    public String jsonValue() {
        return this.name().toLowerCase();
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}