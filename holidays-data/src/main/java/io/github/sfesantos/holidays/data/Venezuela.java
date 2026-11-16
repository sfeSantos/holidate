package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code VE.json} as Java constants; see {@link Spain}.
 */
public final class Venezuela {

    public static final Layer NATIONAL = BundledDataProvider.require("VE");

    private Venezuela() {}
}
