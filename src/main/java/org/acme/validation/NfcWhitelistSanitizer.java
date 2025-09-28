package org.acme.validation;

import io.quarkus.logging.Log;

import java.text.Normalizer;
import java.util.regex.Pattern;

import static java.lang.Character.*;

public class NfcWhitelistSanitizer {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HSPACE = Pattern.compile("[\\p{Zs}\\t\\x0B\\f]+");
    private static final Pattern EXCESS_LF = Pattern.compile("\\n{3,}");

    private NfcWhitelistSanitizer() {
    }

    public static String sanitize(String field, String input) {
        return sanitize(field, input, false, Normalizer.Form.NFKC);
    }

    public static String sanitize(String field, String input, boolean preserveNewlines) {
        return sanitize(field, input, preserveNewlines, Normalizer.Form.NFKC);
    }

    public static String sanitize(String field, String input, boolean preserveNewlines, Normalizer.Form form) {
        if (input == null) return null;

        var s = input;

        if (!Normalizer.isNormalized(s, form)) {
            s = Normalizer.normalize(s, form);
        }

        s = s.strip();

        if (preserveNewlines) {
            // Canonicalize CRLF/CR to LF and collapse horizontal whitespace
            s = s.replace("\r\n", "\n").replace('\r', '\n');
            s = EXCESS_LF.matcher(s).replaceAll("\n\n");
            s = HSPACE.matcher(s).replaceAll(" ");
        } else {
            // Collapse all whitespace (including newlines) to a single space
            s = WHITESPACE.matcher(s).replaceAll(" ");
        }

        var sb = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> {
            if (isExplicitlyDisallowed(cp)) {
                return;
            }

            if (isAllowed(cp)) {
                sb.appendCodePoint(cp);
            }
        });

        var result = sb.toString();
        if (Log.isDebugEnabled() && !result.equals(input)) {
            Log.debug("Sanitized field: " + field);
        }
        return result;
    }

    private static boolean isExplicitlyDisallowed(int cp) {
        return cp == '<' || cp == '>';
    }

    @SuppressWarnings("UnicodeEscape")
    private static boolean isAllowed(int cp) {
        // Explicit allowances not covered by categories
        if (cp == ' ' || cp == '\n') return true;

        return switch (getType(cp)) {
            case UPPERCASE_LETTER, LOWERCASE_LETTER, TITLECASE_LETTER,
                 MODIFIER_LETTER, OTHER_LETTER, DECIMAL_DIGIT_NUMBER, CONNECTOR_PUNCTUATION, DASH_PUNCTUATION,
                 START_PUNCTUATION, END_PUNCTUATION, INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION,
                 OTHER_PUNCTUATION, MATH_SYMBOL, SPACE_SEPARATOR, CURRENCY_SYMBOL, MODIFIER_SYMBOL -> true;

            default -> false;
        };
    }
}
