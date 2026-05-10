package com.firstclub.membership.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum SubscriptionStatus {
    ACTIVE, CANCELLED, EXPIRED, PENDING;

    @Converter(autoApply = true)
    public static class PersistenceConverter implements AttributeConverter<SubscriptionStatus, String> {
        @Override
        public String convertToDatabaseColumn(SubscriptionStatus status) {
            return status != null ? status.name().toLowerCase() : null;
        }

        @Override
        public SubscriptionStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? SubscriptionStatus.valueOf(dbData.toUpperCase()) : null;
        }
    }
}
