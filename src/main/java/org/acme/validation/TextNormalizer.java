package org.acme.validation;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TextNormalizer {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HSPACE = Pattern.compile("[\\p{Zs}\\t\\x0B\\f]+");
    private static final Pattern EXCESS_LF = Pattern.compile("\\n{3,}");
    private static final Pattern HSPACE_AROUND_LF = Pattern.compile("[\\p{Zs}\\t\\x0B\\f]*\\n[\\p{Zs}\\t\\x0B\\f]*");

    private TextNormalizer() {
    }

    public static String normalize(String input) {
        return normalize(input, false);
    }

    public static String normalize(String input, boolean preserveWhitespace) {
        return normalize(input, preserveWhitespace, Normalizer.Form.NFC);
    }

    public static String normalize(String input, boolean preserveNewlines, Normalizer.Form form) {
        if (input == null) return null;

        var s = Normalizer.isNormalized(input, form)
                ? input
                : Normalizer.normalize(input, form);

        s = s.strip();

        if (preserveNewlines) {
            // Canonicalize CRLF/CR to LF and collapse horizontal whitespace
            s = s.replace("\r\n", "\n").replace('\r', '\n');
            s = EXCESS_LF.matcher(s).replaceAll("\n\n");
            s = HSPACE.matcher(s).replaceAll(" ");
            s = HSPACE_AROUND_LF.matcher(s).replaceAll("\n");
        } else {
            // Collapse all whitespace (including newlines) to a single space
            s = WHITESPACE.matcher(s).replaceAll(" ");
        }

        return s;
    }
}