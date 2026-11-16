package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code SI.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Slovenia.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Slovenia {

    public static final Layer NATIONAL = BundledDataProvider.require("SI");

    private Slovenia() {}
}
