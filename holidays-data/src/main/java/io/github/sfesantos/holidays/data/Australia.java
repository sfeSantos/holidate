package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code AU.json} as Java constants, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Australia.NATIONAL, Australia.VICTORIA)}.
 * The data stays in the JSON file; these only read it. {@code NATIONAL}
 * holds the days every state and territory observes; regional and local
 * days are not part of the pack.
 */
public final class Australia {

    public static final Layer NATIONAL = BundledDataProvider.require("AU");
    public static final Layer AUSTRALIAN_CAPITAL_TERRITORY = BundledDataProvider.require("AU-ACT");
    public static final Layer NEW_SOUTH_WALES = BundledDataProvider.require("AU-NSW");
    public static final Layer NORTHERN_TERRITORY = BundledDataProvider.require("AU-NT");
    public static final Layer QUEENSLAND = BundledDataProvider.require("AU-QLD");
    public static final Layer SOUTH_AUSTRALIA = BundledDataProvider.require("AU-SA");
    public static final Layer TASMANIA = BundledDataProvider.require("AU-TAS");
    public static final Layer VICTORIA = BundledDataProvider.require("AU-VIC");
    public static final Layer WESTERN_AUSTRALIA = BundledDataProvider.require("AU-WA");

    private Australia() {}
}
