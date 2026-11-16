package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code HR.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Croatia.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Croatia {

    public static final Layer NATIONAL = BundledDataProvider.require("HR");

    private Croatia() {}
}
