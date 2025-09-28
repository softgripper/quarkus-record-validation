package org.acme.validation;

import io.quarkus.logging.Log;

import java.text.Normalizer;
import java.util.regex.Pattern;

import static java.lang.Character.*;

public class NfcWhitelistSanitizer {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HSPACE = Pattern.compile("[\\p{Zs}\\t\\x0B\\f]+");
    private static final Pattern MULTI_LF = Pattern.compile("\\n{2,}");

    private NfcWhitelistSanitizer() {
    }

    public static String sanitize(String field, String input) {
        return sanitize(field, input, false);
    }

    public static String sanitize(String field, String input, boolean preserveNewlines) {
        if (input == null) return null;

        var s = input;

        if (!Normalizer.isNormalized(s, Normalizer.Form.NFC)) {
            s = Normalizer.normalize(s, Normalizer.Form.NFC);
        }

        s = s.trim();

        if (preserveNewlines) {
            // Canonicalize CRLF/CR to LF and collapse horizontal whitespace
            s = s.replace("\r\n", "\n").replace('\r', '\n');
            s = MULTI_LF.matcher(s).replaceAll("\n");
            s = HSPACE.matcher(s).replaceAll(" ");
        } else {
            // Collapse all whitespace (including newlines) to a single space
            s = WHITESPACE.matcher(s).replaceAll(" ");
        }

        var sb = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> {
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

    @SuppressWarnings("unicode")
    // Emulates the ICU UnicodeSet: [[:L:][:Nd:][:P:][:Sc:]\u0020 \n +]
    private static boolean isAllowed(int cp) {
        // Explicit allowances not covered by categories
        if (cp == ' ' || cp == '\n' || cp == '+') return true;

        // L* (letters), Nd (decimal digits), P* (punctuation), Sc (currency)
        return switch (getType(cp)) {
            // [:L:]
            case UPPERCASE_LETTER, LOWERCASE_LETTER, TITLECASE_LETTER,
                 MODIFIER_LETTER, OTHER_LETTER -> true;

            // [:Nd:]
            case DECIMAL_DIGIT_NUMBER -> true;

            // [:P:]
            case CONNECTOR_PUNCTUATION, DASH_PUNCTUATION, START_PUNCTUATION,
                 END_PUNCTUATION, INITIAL_QUOTE_PUNCTUATION, FINAL_QUOTE_PUNCTUATION,
                 OTHER_PUNCTUATION -> true;

            // [:Sc:]
            case CURRENCY_SYMBOL -> true;
            default -> false;
        };
    }
}
