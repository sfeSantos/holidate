package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code PY.json} as Java constants; see {@link Spain}.
 */
public final class Paraguay {

    public static final Layer NATIONAL = BundledDataProvider.require("PY");

    private Paraguay() {}
}
