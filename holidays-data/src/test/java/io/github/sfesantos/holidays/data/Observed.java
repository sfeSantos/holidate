package io.github.sfesantos.holidays.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.registry.CalendarRegistry;

/** The observed dates of a year, sorted, as an official calendar lists them. */
final class Observed {

    static final CalendarRegistry REGISTRY = CalendarRegistry.load();

    private Observed() {}

    static List<LocalDate> datesIn(String layerId, int year) {
        return REGISTRY.calendar(layerId)
                .holidaysIn(Year.of(year))
                .stream()
                .map(HolidayOccurrence::observed)
                .sorted()
                .toList();
    }

    /** Regions that never move a holiday drop the ones that land on a Sunday from their published list. */
    static List<LocalDate> weekdayDatesIn(String layerId, int year) {
        return datesIn(layerId, year)
                .stream()
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .toList();
    }

    static HolidayCalendar calendar(String layerId) {
        return REGISTRY.calendar(layerId);
    }

    static LocalDate d(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
