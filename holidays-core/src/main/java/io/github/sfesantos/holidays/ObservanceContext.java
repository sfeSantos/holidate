package io.github.sfesantos.holidays;

import java.time.LocalDate;

/**
 * What an {@link ObservanceRule} may ask about the calendar while moving a date.
 * {@link #isHoliday} reports dates already taken by another holiday in the same year.
 */
public interface ObservanceContext {

    boolean isWeekend(LocalDate date);
    boolean isHoliday(LocalDate date);

    default boolean isNonWorking(LocalDate date) {
        return isWeekend(date) || isHoliday(date);
    }
}
