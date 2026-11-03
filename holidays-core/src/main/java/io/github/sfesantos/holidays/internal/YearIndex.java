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
    private final HolidayOccurrence[] byDay;
    private final HolidayOccurrence[] byObserved;
    private final int[] observedDays;
    private final int[] businessDaysBefore;
    private final List<HolidayOccurrence> occurrences;

    YearIndex(int year, int length, long[] anyHoliday, long[] nonWorking, HolidayOccurrence[] byDay,
            HolidayOccurrence[] byObserved, int[] businessDaysBefore, List<HolidayOccurrence> occurrences) {
        this.year = year;
        this.length = length;
        this.anyHoliday = anyHoliday;
        this.nonWorking = nonWorking;
        this.byDay = byDay;
        this.byObserved = byObserved;
        this.observedDays = daysOf(byObserved);
        this.businessDaysBefore = businessDaysBefore;
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

    /** The first holiday observed on that day, or null. */
    public HolidayOccurrence holidayOn(int dayOfYear) {
        return byDay[dayOfYear];
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

    /** Business days strictly before the given day of year. */
    public int businessDaysBefore(int dayOfYear) {
        return businessDaysBefore[dayOfYear];
    }

    public int businessDaysInYear() {
        return businessDaysBefore[length + 1];
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
