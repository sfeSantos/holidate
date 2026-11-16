package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code AT.json} as a Java constant: the holidays of § 7
 * Arbeitsruhegesetz, which apply in all nine Bundesländer.
 */
public final class Austria {

    public static final Layer NATIONAL = BundledDataProvider.require("AT");

    private Austria() {}
}
