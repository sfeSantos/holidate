package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code MT.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Malta.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Malta {

    public static final Layer NATIONAL = BundledDataProvider.require("MT");

    private Malta() {}
}
