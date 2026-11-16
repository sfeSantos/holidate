package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code NI.json} as Java constants; see {@link Spain}.
 */
public final class Nicaragua {

    public static final Layer NATIONAL = BundledDataProvider.require("NI");

    private Nicaragua() {}
}
