package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code IE.json} as Java constants; see {@link Spain}.
 */
public final class Ireland {

    public static final Layer NATIONAL = BundledDataProvider.require("IE");

    private Ireland() {}
}
