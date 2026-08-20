package com.finops.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("0123456789abcdef0123456789abcdef");
    }

    @Test
    void testEncryptDecryptSuccess() {
        String plainText = "mySuperSecretAwsKey!";
        String encrypted = encryptionUtil.encrypt(plainText);
        
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testNullValues() {
        assertNull(encryptionUtil.encrypt(null));
        assertNull(encryptionUtil.decrypt(null));
    }

    @Test
    void testInvalidKeyLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new EncryptionUtil("invalid_length"));
    }

    private void assertNotNull(String str) {
        if (str == null) throw new AssertionError("Expected non-null string");
    }
}
