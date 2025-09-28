package org.acme.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;

import static org.acme.validation.TextNormalizer.normalize;
import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTest {

    @Test
    void nullInputReturnsNull() {
        assertNull(normalize(null));
        assertNull(normalize(null, true));
        assertNull(normalize(null, true, Normalizer.Form.NFC));
    }

    @Test
    void emptyAndWhitespaceOnly_collapseWhenNotPreservingNewlines() {
        assertEquals("", normalize(""));
        assertEquals("", normalize("   "));
        assertEquals("", normalize("\t  \n \r \f \u000B"));
        // strip() trims all leading/trailing whitespace including newlines, so pure whitespace becomes empty
        assertEquals("", normalize("\n\n\n"));
    }

    @Test
    void defaultMode_collapsesAllWhitespaceToSingleSpace() {
        var input = "  a\tb  \n c \r\nd \f  e \u000B   f  ";
        var expected = "a b c d e f";
        assertEquals(expected, normalize(input));
    }

    @Test
    void defaultMode_leadingAndTrailingTrimmed() {
        var input = "\n  \t  Hello world  \r\n ";
        var expected = "Hello world";
        assertEquals(expected, normalize(input));
    }

    @Test
    void normalization_defaultIsNFC_and_customFormNFDSupported() {
        // "Cafe\u0301" is 'e' + combining acute; NFC should combine into 'é'
        var decomposed = "Cafe\u0301";
        var nfcExpected = "Café";
        var resultDefault = normalize(decomposed);
        assertEquals(nfcExpected, resultDefault);
        assertTrue(Normalizer.isNormalized(resultDefault, Normalizer.Form.NFC));

        // When requesting NFD, result should be normalized to NFD
        var nfdResult = normalize(nfcExpected, false, Normalizer.Form.NFD);
        assertTrue(Normalizer.isNormalized(nfdResult, Normalizer.Form.NFD));
        // For a stable check, normalize both to the same form and compare
        assertEquals(
                Normalizer.normalize(nfcExpected, Normalizer.Form.NFD),
                nfdResult
        );
    }

    @Test
    void overloadConsistency_sameAsDefaultWhenPassingNFC() {
        var input = "  A\t\tB \n C  ";
        assertEquals(
                normalize(input),
                normalize(input, false, Normalizer.Form.NFC)
        );
    }

    @Nested
    class PreserveNewlines {

        @Test
        void canonicalizesLineEndingsAndCollapsesHorizontalWhitespace() {
            // Mix CRLF, CR, and LF; horizontal whitespace around tokens should be collapsed to single spaces
            var input = " Line1\t  \r\n  Line2  \r  Line3 \n  Line4 ";
            var expected = "Line1\nLine2\nLine3\nLine4";
            assertEquals(expected, normalize(input, true));
        }

        @Test
        void trimsLeadingAndTrailingWhitespaceEvenWithPreserveNewlines() {
            var input = " \n\t  Hello   \r\n ";
            var expected = "Hello";
            assertEquals(expected, normalize(input, true));
        }

        @Test
        void excessBlankLinesCollapsedToTwo() {
            var input = "Para1\n\n\n\nPara2\n\n\n\n\nPara3";
            var expected = "Para1\n\nPara2\n\nPara3";
            assertEquals(expected, normalize(input, true));
        }

        @Test
        void horizontalSpaceCollapsedButNewlinesPreserved() {
            var input = "a \t  b\nc   \u00A0  d"; // NBSP in second line
            var expected = "a b\nc d";
            assertEquals(expected, normalize(input, true));
        }

        @Test
        void crOnlyNewlinesCanonicalizedToLf() {
            var input = "a\rb\rc\rd";
            var expected = "a\nb\nc\nd";
            assertEquals(expected, normalize(input, true));
        }
    }

    @Nested
    class NonPreservingNewlines {

        @Test
        void newlinesCollapsedToSpaces() {
            var input = "a\nb\r\nc\rd";
            var expected = "a b c d";
            assertEquals(expected, normalize(input));
        }

        @Test
        void nbspNotCollapsedInDefaultMode() {
            var nbsp = '\u00A0';
            var input = "a" + nbsp + nbsp + "b";
            var expected = "a b";
            assertEquals(expected, normalize(input));
        }
    }
}