package org.acme.validation;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TextNormalizer {
    // Collapse any (Unicode-aware) whitespace when not preserving newlines
    private static final Pattern WHITESPACE =
            Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    // Horizontal whitespace (no newlines)
    private static final Pattern HSPACE =
            Pattern.compile("[\\p{Zs}\\t\\x0B\\f]+", Pattern.UNICODE_CHARACTER_CLASS);

    // 3+ linefeeds -> exactly two
    private static final Pattern EXCESS_LF = Pattern.compile("\\n{3,}");

    // Strip horizontal whitespace around linefeeds
    private static final Pattern HSPACE_AROUND_LF =
            Pattern.compile("[\\p{Zs}\\t\\x0B\\f]*\\n[\\p{Zs}\\t\\x0B\\f]*", Pattern.UNICODE_CHARACTER_CLASS);

    // Canonicalize all common line breaks to '\n' (CRLF, CR, NEL, LS, PS)
    private static final Pattern ANY_LINEBREAK = Pattern.compile("\\r\\n|\\r|\\u0085|\\u2028|\\u2029");

    private TextNormalizer() {
    }

    public static String normalize(String input) {
        return normalize(input, false);
    }

    public static String normalize(String input, boolean preserveNewlines) {
        return normalize(input, preserveNewlines, Normalizer.Form.NFC);
    }

    public static String normalize(String input, boolean preserveNewlines, Normalizer.Form form) {
        if (input == null) return null;

        var s = Normalizer.isNormalized(input, form)
                ? input
                : Normalizer.normalize(input, form);

        s = s.strip();

        if (preserveNewlines) {
            // Canonicalize all common line breaks to LF
            s = ANY_LINEBREAK.matcher(s).replaceAll("\n");

            // Collapse runs of 3+ LFs to exactly two
            s = EXCESS_LF.matcher(s).replaceAll("\n\n");

            // Collapse horizontal whitespace to a single space
            s = HSPACE.matcher(s).replaceAll(" ");

            // Remove horizontal whitespace around LFs
            s = HSPACE_AROUND_LF.matcher(s).replaceAll("\n");
        } else {
            // Collapse all whitespace (Unicode-aware) to a single space
            s = WHITESPACE.matcher(s).replaceAll(" ");
        }

        return s;
    }
}