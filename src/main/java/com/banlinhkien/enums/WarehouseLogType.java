package com.banlinhkien.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum WarehouseLogType {
    IMPORT("import"),
    EXPORT("export");

    private final String dbValue;

    WarehouseLogType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static WarehouseLogType fromDbValue(String val) {
        if (val == null) return null;
        for (WarehouseLogType t : values()) {
            if (t.dbValue.equalsIgnoreCase(val)) return t;
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class ConverterImpl implements AttributeConverter<WarehouseLogType, String> {
        @Override
        public String convertToDatabaseColumn(WarehouseLogType attribute) {
            return attribute != null ? attribute.getDbValue() : null;
        }

        @Override
        public WarehouseLogType convertToEntityAttribute(String dbData) {
            return dbData != null ? fromDbValue(dbData) : null;
        }
    }
}
