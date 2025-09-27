package org.acme;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;
import io.quarkus.logging.Log;

import java.util.regex.Pattern;

public class NfcWhitelistSanitizer {
    // Allow: letters, digits, newline, plus, minus, (), {}, []
    // [:L:] = letters, [:Nd:] = decimal digits, [:P:] = punctuation and [:Sc:] currency, u0020 = space
    private static final UnicodeSet ALLOWED = new UnicodeSet(
            "[[:L:][:Nd:][:P:][:Sc:]\\u0020\\n+]"
    ).freeze();

    private static final Normalizer2 NFC = Normalizer2.getNFCInstance();

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

        var span = NFC.spanQuickCheckYes(s);
        if (span != s.length()) {
            s = NFC.normalize(s);
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
            if (ALLOWED.contains(cp)) {
                sb.appendCodePoint(cp);
            }
        });

        var result = sb.toString();
        if (Log.isDebugEnabled() && !result.equals(input)) {
            Log.debug("Sanitized field: " + field);
        }
        return result;
    }
}
