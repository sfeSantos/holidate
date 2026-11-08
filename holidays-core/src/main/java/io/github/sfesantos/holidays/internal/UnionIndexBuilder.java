package io.github.sfesantos.holidays.internal;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;

/**
 * Builds the {@link YearIndex} of a union: holiday bits are OR-ed, business
 * day bits AND-ed. Members built by this library hand over their index;
 * any other {@link HolidayCalendar} is read through its public queries.
 * Ties on the same day keep member order, so {@code holidayOn} prefers
 * the first member.
 */
final class UnionIndexBuilder {

    private static final Comparator<HolidayOccurrence> BY_OBSERVED = Comparator.comparing(HolidayOccurrence::observed);
    private static final Comparator<HolidayOccurrence> BY_ORIGINAL = Comparator.comparing(HolidayOccurrence::original);

    private final List<HolidayCalendar> members;

    UnionIndexBuilder(List<HolidayCalendar> members) {
        this.members = members;
    }

    YearIndex build(int year) {
        int length = Year.isLeap(year) ? 366 : 365;
        var anyHoliday = new long[YearIndex.WORDS];
        var nonWorking = new long[YearIndex.WORDS];
        var businessDays = allDays(length);
        var observed = new ArrayList<HolidayOccurrence>();
        var original = new ArrayList<HolidayOccurrence>();

        for (var member : members) {
            var part = MemberYear.of(member, year, length);
            or(anyHoliday, part.anyHoliday());
            or(nonWorking, part.nonWorking());
            and(businessDays, part.businessDays());
            observed.addAll(Arrays.asList(part.byObserved()));
            original.addAll(part.occurrences());
        }

        observed.sort(BY_OBSERVED);
        original.sort(BY_ORIGINAL);

        return new YearIndex(year, length, anyHoliday, nonWorking, businessDays,
                observed.toArray(HolidayOccurrence[]::new), original);
    }

    private static long[] allDays(int length) {
        var bits = new long[YearIndex.WORDS];

        for (int d = 1; d <= length; d++) {
            YearIndex.set(bits, d);
        }

        return bits;
    }

    private static void or(long[] into, long[] bits) {
        for (int i = 0; i < into.length; i++) {
            into[i] |= bits[i];
        }
    }

    private static void and(long[] into, long[] bits) {
        for (int i = 0; i < into.length; i++) {
            into[i] &= bits[i];
        }
    }

    /** One member's view of a year, taken from its index when it has one. */
    private record MemberYear(long[] anyHoliday, long[] nonWorking, long[] businessDays,
            HolidayOccurrence[] byObserved, List<HolidayOccurrence> occurrences) {

        static MemberYear of(HolidayCalendar member, int year, int length) {
            if (member instanceof IndexedCalendar indexed) {
                var index = indexed.index(year);

                return new MemberYear(index.anyHolidayBits(), index.nonWorkingBits(), index.businessDayBits(),
                        index.byObserved(), index.occurrences());
            }

            return queried(member, year, length);
        }

        private static MemberYear queried(HolidayCalendar member, int year, int length) {
            var first = LocalDate.ofYearDay(year, 1);
            var byObserved = member.holidaysBetween(first, first.plusYears(1)).toArray(HolidayOccurrence[]::new);
            var anyHoliday = new long[YearIndex.WORDS];
            var businessDays = new long[YearIndex.WORDS];
            var nonWorking = new long[YearIndex.WORDS];

            for (var occurrence : byObserved) {
                YearIndex.set(anyHoliday, occurrence.observed().getDayOfYear());
            }

            for (int d = 1; d <= length; d++) {
                if (member.isBusinessDay(LocalDate.ofYearDay(year, d))) {
                    YearIndex.set(businessDays, d);
                }
            }

            for (int i = 0; i < nonWorking.length; i++) {
                nonWorking[i] = anyHoliday[i] & ~businessDays[i];
            }

            return new MemberYear(anyHoliday, nonWorking, businessDays, byObserved, member.holidaysIn(Year.of(year)));
        }
    }
}
