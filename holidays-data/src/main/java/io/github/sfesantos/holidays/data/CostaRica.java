package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code CR.json} as Java constants; see {@link Spain}.
 */
public final class CostaRica {

    public static final Layer NATIONAL = BundledDataProvider.require("CR");

    private CostaRica() {}
}
