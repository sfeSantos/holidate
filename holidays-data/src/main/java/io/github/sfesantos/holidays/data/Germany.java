package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code DE.json} as Java constants. Germany has one federal
 * holiday, 3 October; the rest is state law. {@code NATIONAL} holds the nine
 * days every Land observes and each Land adds its own. Holidays that apply
 * only in some municipalities of a Land are not included.
 */
public final class Germany {

    public static final Layer NATIONAL = BundledDataProvider.require("DE");
    public static final Layer BADEN_WURTTEMBERG = BundledDataProvider.require("DE-BW");
    public static final Layer BAVARIA = BundledDataProvider.require("DE-BY");
    public static final Layer BERLIN = BundledDataProvider.require("DE-BE");
    public static final Layer BRANDENBURG = BundledDataProvider.require("DE-BB");
    public static final Layer BREMEN = BundledDataProvider.require("DE-HB");
    public static final Layer HAMBURG = BundledDataProvider.require("DE-HH");
    public static final Layer HESSE = BundledDataProvider.require("DE-HE");
    public static final Layer MECKLENBURG_WESTERN_POMERANIA = BundledDataProvider.require("DE-MV");
    public static final Layer LOWER_SAXONY = BundledDataProvider.require("DE-NI");
    public static final Layer NORTH_RHINE_WESTPHALIA = BundledDataProvider.require("DE-NW");
    public static final Layer RHINELAND_PALATINATE = BundledDataProvider.require("DE-RP");
    public static final Layer SAARLAND = BundledDataProvider.require("DE-SL");
    public static final Layer SAXONY = BundledDataProvider.require("DE-SN");
    public static final Layer SAXONY_ANHALT = BundledDataProvider.require("DE-ST");
    public static final Layer SCHLESWIG_HOLSTEIN = BundledDataProvider.require("DE-SH");
    public static final Layer THURINGIA = BundledDataProvider.require("DE-TH");

    private Germany() {}
}
