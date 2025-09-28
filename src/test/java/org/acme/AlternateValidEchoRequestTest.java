package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class AlternateValidEchoRequestTest {
    @Test
    void constructorCallsValidate() {
        assertThrows(ConstraintViolationException.class, () -> new AlternateValidEchoRequest("", "12345"));
    }
}
