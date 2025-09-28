package org.acme.validation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;

import static org.acme.validation.NfcWhitelistSanitizer.sanitize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class NfcWhitelistSanitizerTest {

    @Test
    void nullInputReturnsNull() {
        assertNull(sanitize("field", null));
    }

    @Test
    void trimsAndCollapsesWhitespaceWithoutPreservingNewlines() {
        var input = " \tHello \n  World \r\n!  ";
        var result = sanitize("field", input);
        // All whitespace collapsed to single spaces and leading/trailing whitespace trimmed
        assertEquals("Hello World !", result);
    }

    @Test
    void preservesNewlinesAndCanonicalizesWhenRequested() {
        var input = "  A\tB\r\nC   D\rE  ";
        var result = sanitize("field", input, true);
        // CRLF / CR -> LF, MULTI_LF collapsed, horizontal whitespace collapsed to single spaces
        assertEquals("A B\nC D\nE", result);
    }

    @Test
    void collapsesMultipleNewlinesToDoubleWhenPreserving() {
        var input = "a\r\n\r\n\r\nb";
        var result = sanitize("field", input, true);
        assertEquals("a\n\nb", result);
    }

    @Test
    void removesDisallowedCharactersButKeepsLettersDigitsPunctuationCurrency() {
        // Emoji (So) and ZWJ (Cf) should be removed; punctuation and currency kept
        var input = "A🙂B 1€-[]{}()";
        var result = sanitize("field", input);
        assertEquals("AB 1€-[]{}()", result);
    }

    @Test
    void nfcNormalizationOfDecomposedCharacters() {
        // "Cafe\u0301" should normalize to "Café"
        var input = "Cafe\u0301";
        var result = sanitize("name", input);
        assertEquals("Café", result);
    }

    @Test
    void newlinesAreCollapsedToSpaceWhenNotPreserving() {
        var input = "a\n\nb";
        var result = sanitize("field", input);
        assertEquals("a b", result);
    }

    @Test
    void highCodePointsLettersAreKeptEmojiRemoved() {
        // Gothic letter U+10348 is a Letter (Lo) and should be kept; emoji should be removed
        var input = "X𐍈🙂Y";
        var result = sanitize("field", input);
        assertEquals("X𐍈Y", result);
    }

    @Test
    void allowEmoji() {
        var input = "🙂";
        var result = sanitize("field", input, false, Normalizer.Form.NFKC, true);
        assertEquals("🙂", result);
    }

    @Test
    void idempotentSanitization() {
        var input = "  A\tB\r\nC🙂D\u200D€  ";
        var once = sanitize("field", input, true);
        var twice = sanitize("field", once, true);
        assertEquals(once, twice);
    }

    @Test
    void greaterThanLessThanRemoved() {
        var input = "<>";
        var result = sanitize("field", input);
        assertEquals("", result);
    }

    @Test
    void basicExpectedCharacters() {
        var input = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 \n-=!@#$%^&*()_+[]\\{}|;':\",./?";
        var result = sanitize("field", input, true);
        assertEquals(input, result);
    }
}
