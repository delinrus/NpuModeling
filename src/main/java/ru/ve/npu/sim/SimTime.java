package ru.ve.npu.sim;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Immutable time representation for NPU simulation using nanoseconds precision.
 * Provides type-safe time operations and conversions.
 */
public record SimTime(long nanos) implements Comparable<SimTime> {

    /**
     * Zero time constant.
     */
    public static final SimTime ZERO = new SimTime(0L);

    /**
     * Creates SimTime from nanoseconds.
     */
    public static SimTime ofNanos(long nanos) {
        return new SimTime(nanos);
    }

    /**
     * Creates SimTime from microseconds.
     */
    public static SimTime ofMicros(long micros) {
        return new SimTime(micros * 1_000L);
    }

    /**
     * Creates SimTime from milliseconds.
     */
    public static SimTime ofMillis(long millis) {
        return new SimTime(millis * 1_000_000L);
    }

    /**
     * Creates SimTime from seconds.
     */
    public static SimTime ofSeconds(long seconds) {
        return new SimTime(seconds * 1_000_000_000L);
    }

    /**
     * Creates SimTime from fractional seconds.
     */
    public static SimTime ofSeconds(double seconds) {
        return new SimTime((long) (seconds * 1_000_000_000L));
    }

    /**
     * Creates SimTime from Duration.
     */
    public static SimTime of(Duration duration) {
        return new SimTime(duration.toNanos());
    }

    /**
     * Adds another SimTime to this one.
     */
    public SimTime plus(SimTime other) {
        return new SimTime(this.nanos + other.nanos);
    }

    /**
     * Adds nanoseconds to this SimTime.
     */
    public SimTime plusNanos(long nanos) {
        return new SimTime(this.nanos + nanos);
    }

    /**
     * Adds milliseconds to this SimTime.
     */
    public SimTime plusMillis(long millis) {
        return new SimTime(this.nanos + millis * 1_000_000L);
    }

    /**
     * Adds seconds to this SimTime.
     */
    public SimTime plusSeconds(long seconds) {
        return new SimTime(this.nanos + seconds * 1_000_000_000L);
    }

    /**
     * Adds fractional seconds to this SimTime.
     */
    public SimTime plusSeconds(double seconds) {
        return new SimTime(this.nanos + (long) (seconds * 1_000_000_000L));
    }

    /**
     * Subtracts another SimTime from this one.
     */
    public SimTime minus(SimTime other) {
        return new SimTime(this.nanos - other.nanos);
    }

    /**
     * Subtracts nanoseconds from this SimTime.
     */
    public SimTime minusNanos(long nanos) {
        return new SimTime(this.nanos - nanos);
    }

    /**
     * Subtracts milliseconds from this SimTime.
     */
    public SimTime minusMillis(long millis) {
        return new SimTime(this.nanos - millis * 1_000_000L);
    }

    /**
     * Subtracts seconds from this SimTime.
     */
    public SimTime minusSeconds(long seconds) {
        return new SimTime(this.nanos - seconds * 1_000_000_000L);
    }

    /**
     * Multiplies this SimTime by a factor.
     */
    public SimTime multiply(double factor) {
        return new SimTime((long) (this.nanos * factor));
    }

    /**
     * Divides this SimTime by a factor.
     */
    public SimTime divide(double factor) {
        return new SimTime((long) (this.nanos / factor));
    }

    /**
     * Converts to Duration.
     */
    public Duration toDuration() {
        return Duration.ofNanos(nanos);
    }

    /**
     * Converts to nanoseconds.
     */
    public long toNanos() {
        return nanos;
    }

    /**
     * Converts to microseconds.
     */
    public long toMicros() {
        return nanos / 1_000L;
    }

    /**
     * Converts to milliseconds.
     */
    public long toMillis() {
        return nanos / 1_000_000L;
    }

    /**
     * Converts to seconds as long.
     */
    public long toSeconds() {
        return nanos / 1_000_000_000L;
    }

    /**
     * Converts to fractional seconds as double.
     */
    public double toSecondsDouble() {
        return nanos / 1_000_000_000.0;
    }

    /**
     * Converts to specified TimeUnit.
     */
    public long to(TimeUnit unit) {
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Checks if this time is before another time.
     */
    public boolean isBefore(SimTime other) {
        return this.nanos < other.nanos;
    }
    
    /**
     * Checks if this time is before or equal to another time.
     */
    public boolean isBeforeOrEqual(SimTime other) {
        return this.nanos <= other.nanos;
    }

    /**
     * Checks if this time is after another time.
     */
    public boolean isAfter(SimTime other) {
        return this.nanos > other.nanos;
    }
    
    /**
     * Checks if this time is after or equal to another time.
     */
    public boolean isAfterOrEqual(SimTime other) {
        return this.nanos >= other.nanos;
    }

    /**
     * Checks if this time is zero.
     */
    public boolean isZero() {
        return nanos == 0L;
    }

    /**
     * Checks if this time is positive.
     */
    public boolean isPositive() {
        return nanos > 0L;
    }

    /**
     * Checks if this time is negative.
     */
    public boolean isNegative() {
        return nanos < 0L;
    }

    /**
     * Returns the absolute value of this time.
     */
    public SimTime abs() {
        return nanos < 0 ? new SimTime(-nanos) : this;
    }

    /**
     * Returns the minimum of this time and another time.
     */
    public SimTime min(SimTime other) {
        return this.nanos <= other.nanos ? this : other;
    }

    /**
     * Returns the maximum of this time and another time.
     */
    public SimTime max(SimTime other) {
        return this.nanos >= other.nanos ? this : other;
    }

    @Override
    public int compareTo(SimTime other) {
        return Long.compare(this.nanos, other.nanos);
    }

    @Override
    public String toString() {
        if (nanos == 0) {
            return "0s";
        }

        // Format based on magnitude for readability
        if (nanos < 1_000L) {
            return nanos + "ns";
        } else if (nanos < 1_000_000L) {
            return String.format("%.3fμs", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000L) {
            return String.format("%.3fms", nanos / 1_000_000.0);
        } else {
            return String.format("%.3fs", nanos / 1_000_000_000.0);
        }
    }

    /**
     * Returns a formatted string with the specified unit.
     */
    public String toString(TimeUnit unit) {
        long value = to(unit);
        return switch (unit) {
            case NANOSECONDS -> value + "ns";
            case MICROSECONDS -> value + "μs";
            case MILLISECONDS -> value + "ms";
            case SECONDS -> value + "s";
            case MINUTES -> value + "min";
            case HOURS -> value + "h";
            case DAYS -> value + "d";
        };
    }
}
