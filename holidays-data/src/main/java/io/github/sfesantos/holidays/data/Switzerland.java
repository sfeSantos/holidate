package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code CH.json} as a Java constant. Swiss holidays are
 * cantonal law, apart from 1 August. {@code NATIONAL} holds only the four
 * days every canton observes (Neujahrstag, Auffahrt, Bundesfeiertag and
 * Weihnachtstag); a calendar for a canton needs its own layer on top.
 */
public final class Switzerland {

    public static final Layer NATIONAL = BundledDataProvider.require("CH");

    private Switzerland() {}
}
