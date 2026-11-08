package io.github.sfesantos.holidays.internal;

import java.util.List;

import io.github.sfesantos.holidays.HolidayOccurrence;

/**
 * Everything the calendar knows about one year, precomputed and frozen.
 * Indexed by day of year (1..366) so the hot queries never touch a map.
 */
public final class YearIndex {

    static final int WORDS = 6;

    private final int year;
    private final int length;
    private final long[] anyHoliday;
    private final long[] nonWorking;
    private final long[] businessDays;
    private final int[] businessDaysBeforeWord;
    private final HolidayOccurrence[] byObserved;
    private final int[] observedDays;
    private final List<HolidayOccurrence> occurrences;

    YearIndex(int year, int length, long[] anyHoliday, long[] nonWorking, long[] businessDays,
            HolidayOccurrence[] byObserved, List<HolidayOccurrence> occurrences) {
        this.year = year;
        this.length = length;
        this.anyHoliday = anyHoliday;
        this.nonWorking = nonWorking;
        this.businessDays = businessDays;
        this.businessDaysBeforeWord = prefixOf(businessDays);
        this.byObserved = byObserved;
        this.observedDays = daysOf(byObserved);
        this.occurrences = List.copyOf(occurrences);
    }

    public int year() {
        return year;
    }

    public int length() {
        return length;
    }

    public boolean isHoliday(int dayOfYear) {
        return bit(anyHoliday, dayOfYear);
    }

    public boolean isNonWorkingHoliday(int dayOfYear) {
        return bit(nonWorking, dayOfYear);
    }

    public boolean isBusinessDay(int dayOfYear) {
        return bit(businessDays, dayOfYear);
    }

    /** The first business day at or after the given day of year, or -1 when the year has none left. */
    public int businessDayAtOrAfter(int dayOfYear) {
        if (dayOfYear > length) {
            return -1;
        }

        int word = dayOfYear >>> 6;
        long bits = businessDays[word] & (-1L << (dayOfYear & 63));

        while (bits == 0) {
            word++;

            if (word == WORDS) {
                return -1;
            }

            bits = businessDays[word];
        }

        return (word << 6) + Long.numberOfTrailingZeros(bits);
    }

    /** The last business day at or before the given day of year, or -1 when the year has none before it. */
    public int businessDayAtOrBefore(int dayOfYear) {
        if (dayOfYear < 1) {
            return -1;
        }

        int day = Math.min(dayOfYear, length);
        int word = day >>> 6;
        long bits = businessDays[word] & (-1L >>> (63 - (day & 63)));

        while (bits == 0) {
            word--;

            if (word < 0) {
                return -1;
            }

            bits = businessDays[word];
        }

        return (word << 6) + 63 - Long.numberOfLeadingZeros(bits);
    }

    /** The day of year of the n-th business day of the year, 1-based; n must be within the year's count. */
    public int nthBusinessDay(int n) {
        int word = 0;

        while (businessDaysBeforeWord[word + 1] < n) {
            word++;
        }

        return (word << 6) + selectBit(businessDays[word], n - businessDaysBeforeWord[word]);
    }

    /** Position of the n-th set bit, 1-based, by halving: six bit counts instead of a scan. */
    private static int selectBit(long bits, int n) {
        int offset = 0;

        for (int width = 32; width > 0; width >>= 1) {
            long lower = bits & ((1L << width) - 1);
            int count = Long.bitCount(lower);

            if (count < n) {
                n -= count;
                bits >>>= width;
                offset += width;
            } else {
                bits = lower;
            }
        }

        return offset;
    }

    /** Index into {@link #byObserved()} of the first holiday observed at or after the day, or -1. */
    public int holidayAtOrAfter(int dayOfYear) {
        int i = observedIndex(dayOfYear);

        return i < observedDays.length ? i : -1;
    }

    /** Position in {@link #byObserved()} where a holiday observed on the day would start; may equal the count. */
    public int observedIndex(int dayOfYear) {
        return lowerBound(observedDays, dayOfYear);
    }

    /** Index into {@link #byObserved()} of the first holiday on the latest observed day before the given day, or -1. */
    public int holidayBefore(int dayOfYear) {
        int i = lowerBound(observedDays, dayOfYear) - 1;

        if (i < 0) {
            return -1;
        }

        int day = observedDays[i];

        while (i > 0 && observedDays[i - 1] == day) {
            i--;
        }

        return i;
    }

    public int holidayCount() {
        return byObserved.length;
    }

    long[] anyHolidayBits() {
        return anyHoliday;
    }

    long[] nonWorkingBits() {
        return nonWorking;
    }

    long[] businessDayBits() {
        return businessDays;
    }

    /** Business days in the words below each word, so a count below any day is one lookup and one bit count. */
    private static int[] prefixOf(long[] businessDays) {
        var prefix = new int[WORDS + 1];

        for (int w = 0; w < WORDS; w++) {
            prefix[w + 1] = prefix[w] + Long.bitCount(businessDays[w]);
        }

        return prefix;
    }

    /** The first holiday observed on that day, or null. */
    public HolidayOccurrence holidayOn(int dayOfYear) {
        int i = lowerBound(observedDays, dayOfYear);

        return i < observedDays.length && observedDays[i] == dayOfYear ? byObserved[i] : null;
    }

    /** Holidays whose original date falls in this year, by original date. */
    public List<HolidayOccurrence> occurrences() {
        return occurrences;
    }

    /** Holidays observed in this year, by observed date; may include spillovers from neighbouring years. */
    public HolidayOccurrence[] byObserved() {
        return byObserved;
    }

    public int[] observedDays() {
        return observedDays;
    }

    /** Business days strictly before the given day of year, which may be one past the year's end. */
    public int businessDaysBefore(int dayOfYear) {
        int word = dayOfYear >>> 6;

        return businessDaysBeforeWord[word] + Long.bitCount(businessDays[word] & ((1L << (dayOfYear & 63)) - 1));
    }

    public int businessDaysInYear() {
        return businessDaysBeforeWord[WORDS];
    }

    /** Smallest index whose value is at least {@code key}, or the array length. */
    private static int lowerBound(int[] sorted, int key) {
        int low = 0;
        int high = sorted.length;

        while (low < high) {
            int mid = (low + high) >>> 1;

            if (sorted[mid] < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return low;
    }

    static boolean bit(long[] words, int dayOfYear) {
        return (words[dayOfYear >>> 6] & (1L << (dayOfYear & 63))) != 0;
    }

    static void set(long[] words, int dayOfYear) {
        words[dayOfYear >>> 6] |= 1L << (dayOfYear & 63);
    }

    private static int[] daysOf(HolidayOccurrence[] byObserved) {
        var days = new int[byObserved.length];

        for (int i = 0; i < days.length; i++) {
            days[i] = byObserved[i].observed().getDayOfYear();
        }

        return days;
    }
}
