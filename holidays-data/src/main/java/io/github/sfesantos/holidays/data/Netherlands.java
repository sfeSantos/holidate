package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code NL.json} as a Java constant: the official holidays the
 * Rijksoverheid lists. Koningsdag is exact from 2025 on except when 27 April
 * is a Sunday (2031, 2036): the real day is then the Saturday before.
 */
public final class Netherlands {

    public static final Layer NATIONAL = BundledDataProvider.require("NL");

    private Netherlands() {}
}
