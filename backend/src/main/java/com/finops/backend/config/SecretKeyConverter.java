package com.finops.backend.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.springframework.stereotype.Component;

import com.finops.backend.util.EncryptionUtil;

@Component
@Converter
public class SecretKeyConverter implements AttributeConverter<String, String> {

    private final EncryptionUtil encryptionUtil;

    public SecretKeyConverter(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptionUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptionUtil.decrypt(dbData);
    }
}
