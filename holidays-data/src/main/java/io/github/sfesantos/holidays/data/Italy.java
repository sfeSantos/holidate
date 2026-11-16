package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code IT.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Italy.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Italy {

    public static final Layer NATIONAL = BundledDataProvider.require("IT");

    private Italy() {}
}
