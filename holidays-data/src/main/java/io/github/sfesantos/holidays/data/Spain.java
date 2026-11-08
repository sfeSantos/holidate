package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code ES.json} as Java constants, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)}.
 * The data stays in the JSON file; these only read it.
 */
public final class Spain {

    public static final Layer NATIONAL = BundledDataProvider.require("ES");
    public static final Layer ANDALUCIA = BundledDataProvider.require("ES-AN");
    public static final Layer MALAGA = BundledDataProvider.require("ES-AN-MA");
    public static final Layer CATALUNYA = BundledDataProvider.require("ES-CT");

    private Spain() {}
}
