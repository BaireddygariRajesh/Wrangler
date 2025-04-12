
package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.Public;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token representing a time duration value (e.g., "150ms", "2.5s", "1m").
 * Stores the value canonically in nanoseconds (long).
 */
@Public
public final class TimeDuration extends Token<Long> {
    // Pattern to capture number and unit (case-insensitive for unit)
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^(-?[0-9]+(?:\\.[0-9]+)?)\\s*([nNuUmMsShHdD][sS]?)$");

    private static final long NANOS_PER_MICRO = 1000L;
    private static final long NANOS_PER_MILLI = NANOS_PER_MICRO * 1000L;
    private static final long NANOS_PER_SECOND = NANOS_PER_MILLI * 1000L;
    private static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * 60L;
    private static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * 60L;
    private static final long NANOS_PER_DAY = NANOS_PER_HOUR * 24L;

    public TimeDuration(String token) {
        super(TokenType.TIME_DURATION, token);
        this.value = parseNanos(token);
    }

    private long parseNanos(String text) {
        Matcher matcher = TIME_PATTERN.matcher(text.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: '" + text +
                    "'. Expected format like '150ms', '2.5s', '1m'.");
        }

        double number = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        long multiplier;
        switch (unit) {
            case "ns": multiplier = 1L; break;
            case "us": multiplier = NANOS_PER_MICRO; break;
            case "ms": multiplier = NANOS_PER_MILLI; break;
            case "s":  multiplier = NANOS_PER_SECOND; break;
            case "m":  multiplier = NANOS_PER_MINUTE; break;
            case "h":  multiplier = NANOS_PER_HOUR; break;
            case "d":  multiplier = NANOS_PER_DAY; break;
            default:
                throw new IllegalArgumentException("Unknown time unit in '" + text + "'");
        }

        double nanos = number * multiplier;
        if (nanos > Long.MAX_VALUE || nanos < Long.MIN_VALUE) {
            throw new ArithmeticException("Time duration '" + text +
                    "' results in nanosecond value outside the range of Long.");
        }

        return Math.round(nanos);
    }

    public long getNanoseconds() {
        return getValue();
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    public static TokenType getTokenType() {
        return TokenType.TIME_DURATION;
    }
}
