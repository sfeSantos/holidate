package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code NL.json} as a Java constant: the official holidays the
 * Rijksoverheid lists, without Koningsdag, whose date the JSON format cannot
 * express.
 */
public final class Netherlands {

    public static final Layer NATIONAL = BundledDataProvider.require("NL");

    private Netherlands() {}
}
