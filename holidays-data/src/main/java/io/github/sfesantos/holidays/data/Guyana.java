package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code GY.json} as Java constants; see {@link Spain}.
 */
public final class Guyana {

    public static final Layer NATIONAL = BundledDataProvider.require("GY");

    private Guyana() {}
}
