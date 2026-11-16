package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code CL.json} as Java constants; see {@link Spain}.
 */
public final class Chile {

    public static final Layer NATIONAL = BundledDataProvider.require("CL");

    private Chile() {}
}
