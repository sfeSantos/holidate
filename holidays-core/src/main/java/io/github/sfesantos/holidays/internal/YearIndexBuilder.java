package io.github.sfesantos.holidays.internal;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.ObservanceContext;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.internal.LayerResolver.ResolvedHoliday;

/**
 * Builds a {@link YearIndex}. Observance can push a holiday into the
 * neighbouring year (1 January on a Saturday observed on 31 December), so
 * the previous and next years are placed too and their spillovers kept.
 */
final class YearIndexBuilder {

    private static final Comparator<HolidayOccurrence> BY_OBSERVED = Comparator.comparing(HolidayOccurrence::observed);
    private static final Comparator<HolidayOccurrence> BY_ORIGINAL = Comparator.comparing(HolidayOccurrence::original);

    private final CompiledCalendar calendar;

    YearIndexBuilder(CompiledCalendar calendar) {
        this.calendar = calendar;
    }

    YearIndex build(int year) {
        var own = place(year);
        var observedHere = new ArrayList<HolidayOccurrence>();
        collectObservedIn(year, place(year - 1), observedHere);
        collectObservedIn(year, own, observedHere);
        collectObservedIn(year, place(year + 1), observedHere);
        observedHere.sort(BY_OBSERVED);
        own.sort(BY_ORIGINAL);

        return assemble(year, observedHere, own);
    }

    private YearIndex assemble(int year, List<HolidayOccurrence> observedHere, List<HolidayOccurrence> own) {
        int length = Year.isLeap(year) ? 366 : 365;
        var anyHoliday = new long[YearIndex.WORDS];
        var nonWorking = new long[YearIndex.WORDS];
        var byDay = new HolidayOccurrence[length + 1];

        for (var occurrence : observedHere) {
            int day = occurrence.observed().getDayOfYear();
            YearIndex.set(anyHoliday, day);

            if (calendar.isNonWorking(occurrence.type())) {
                YearIndex.set(nonWorking, day);
            }

            if (byDay[day] == null) {
                byDay[day] = occurrence;
            }
        }

        var prefix = businessDayPrefix(year, length, nonWorking);

        return new YearIndex(year, length, anyHoliday, nonWorking, byDay,
                observedHere.toArray(HolidayOccurrence[]::new), prefix, own);
    }

    private int[] businessDayPrefix(int year, int length, long[] nonWorking) {
        var prefix = new int[length + 2];
        var weekend = calendar.weekend();
        var day = LocalDate.ofYearDay(year, 1).getDayOfWeek();

        for (int d = 1; d <= length; d++) {
            boolean business = !weekend.contains(day) && !YearIndex.bit(nonWorking, d);
            prefix[d + 1] = prefix[d] + (business ? 1 : 0);
            day = day.plus(1);
        }

        return prefix;
    }

    private List<HolidayOccurrence> place(int year) {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            return List.of();
        }

        var placed = new ArrayList<HolidayOccurrence>();
        var context = new PlacementContext(calendar.weekend(), new HashSet<>());

        for (var resolved : calendar.holidays()) {
            resolved.holiday()
                    .dateIn(Year.of(year), calendar.easter())
                    .ifPresent(original -> placed.add(place(resolved, original, context)));
        }

        return placed;
    }

    private HolidayOccurrence place(ResolvedHoliday resolved, LocalDate original, PlacementContext context) {
        var holiday = resolved.holiday();
        var rule = holiday.inheritsObservance() ? calendar.observance() : holiday.observance();
        var observed = ObservanceChain.observe(rule, original, context);

        if (calendar.isNonWorking(holiday.type())) {
            context.taken().add(observed);
        }

        return new HolidayOccurrence(holiday.name(), original, observed, holiday.type(), resolved.layer());
    }

    private static void collectObservedIn(int year, List<HolidayOccurrence> placed, List<HolidayOccurrence> into) {
        for (var occurrence : placed) {
            if (occurrence.observed().getYear() == year) {
                into.add(occurrence);
            }
        }
    }

    private record PlacementContext(Weekend weekend, Set<LocalDate> taken) implements ObservanceContext {

        @Override
        public boolean isWeekend(LocalDate date) {
            return weekend.contains(date.getDayOfWeek());
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return taken.contains(date);
        }
    }
}
