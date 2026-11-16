package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code SK.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Slovakia.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Slovakia {

    public static final Layer NATIONAL = BundledDataProvider.require("SK");

    private Slovakia() {}
}
