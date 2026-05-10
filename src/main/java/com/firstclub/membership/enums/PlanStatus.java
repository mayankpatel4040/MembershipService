package com.firstclub.membership.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum PlanStatus {
    ACTIVE, INACTIVE;

    @Converter(autoApply = true)
    public static class PersistenceConverter implements AttributeConverter<PlanStatus, String> {
        @Override
        public String convertToDatabaseColumn(PlanStatus status) {
            return status != null ? status.name().toLowerCase() : null;
        }

        @Override
        public PlanStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? PlanStatus.valueOf(dbData.toUpperCase()) : null;
        }
    }
}
