package io.github.sfesantos.holidays;

import java.time.LocalDate;

/**
 * Read-only questions about working days. A business day is neither a
 * weekend day nor a holiday of a non-working type.
 */
public interface BusinessDayQueries {

    Weekend weekend();
    boolean isWeekend(LocalDate date);
    boolean isBusinessDay(LocalDate date);
}
