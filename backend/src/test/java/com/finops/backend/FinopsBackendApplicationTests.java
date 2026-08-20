package com.finops.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FinopsBackendApplicationTests {

    @Test
    void contextLoads() {
        assertNotNull(FinopsBackendApplication.class);
    }
}
