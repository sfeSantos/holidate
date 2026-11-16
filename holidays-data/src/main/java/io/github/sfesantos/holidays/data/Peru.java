package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code PE.json} as Java constants; see {@link Spain}.
 */
public final class Peru {

    public static final Layer NATIONAL = BundledDataProvider.require("PE");

    private Peru() {}
}
