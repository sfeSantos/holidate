package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code FR.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(France.NATIONAL)}. It covers
 * metropolitan France. The data stays in the JSON file; this only reads it.
 */
public final class France {

    public static final Layer NATIONAL = BundledDataProvider.require("FR");

    private France() {}
}
