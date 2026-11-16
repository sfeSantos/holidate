package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code BZ.json} as Java constants; see {@link Spain}.
 */
public final class Belize {

    public static final Layer NATIONAL = BundledDataProvider.require("BZ");

    private Belize() {}
}
