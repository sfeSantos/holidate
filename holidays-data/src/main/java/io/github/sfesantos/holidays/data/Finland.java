package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code FI.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Finland.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Finland {

    public static final Layer NATIONAL = BundledDataProvider.require("FI");

    private Finland() {}
}
