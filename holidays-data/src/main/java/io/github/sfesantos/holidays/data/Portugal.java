package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code PT.json} as Java constants, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Portugal.NATIONAL, Portugal.LISBON)}.
 * The data stays in the JSON file; these only read it.
 */
public final class Portugal {

    public static final Layer NATIONAL = BundledDataProvider.require("PT");
    public static final Layer LISBON = BundledDataProvider.require("PT-11-LIS");
    public static final Layer PORTO = BundledDataProvider.require("PT-13-PRT");

    private Portugal() {}
}
