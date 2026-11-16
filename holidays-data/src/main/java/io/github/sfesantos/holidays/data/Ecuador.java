package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code EC.json} as Java constants; see {@link Spain}.
 */
public final class Ecuador {

    public static final Layer NATIONAL = BundledDataProvider.require("EC");

    private Ecuador() {}
}
