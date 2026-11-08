package io.github.sfesantos.holidays.data;

import java.time.Month;

import io.github.sfesantos.holidays.Holiday;

/**
 * The definitions most countries share, for calendars written in Java.
 * Names are English; a layer that needs the local name builds its own
 * {@link Holiday}, since the name is the holiday's identity across layers.
 */
public final class Holidays {

    public static final Holiday NEW_YEAR = Holiday.fixed("New Year's Day", Month.JANUARY, 1);
    public static final Holiday EPIPHANY = Holiday.fixed("Epiphany", Month.JANUARY, 6);
    public static final Holiday CARNIVAL_MONDAY = Holiday.easterRelative("Carnival Monday", -48);
    public static final Holiday CARNIVAL_TUESDAY = Holiday.easterRelative("Carnival Tuesday", -47);
    public static final Holiday ASH_WEDNESDAY = Holiday.easterRelative("Ash Wednesday", -46);
    public static final Holiday MAUNDY_THURSDAY = Holiday.easterRelative("Maundy Thursday", -3);
    public static final Holiday GOOD_FRIDAY = Holiday.easterRelative("Good Friday", -2);
    public static final Holiday EASTER_SUNDAY = Holiday.easterRelative("Easter Sunday", 0);
    public static final Holiday EASTER_MONDAY = Holiday.easterRelative("Easter Monday", 1);
    public static final Holiday ASCENSION = Holiday.easterRelative("Ascension Day", 39);
    public static final Holiday WHIT_MONDAY = Holiday.easterRelative("Whit Monday", 50);
    public static final Holiday CORPUS_CHRISTI = Holiday.easterRelative("Corpus Christi", 60);
    public static final Holiday LABOUR_DAY = Holiday.fixed("Labour Day", Month.MAY, 1);
    public static final Holiday ASSUMPTION = Holiday.fixed("Assumption of Mary", Month.AUGUST, 15);
    public static final Holiday ALL_SAINTS = Holiday.fixed("All Saints' Day", Month.NOVEMBER, 1);
    public static final Holiday IMMACULATE_CONCEPTION = Holiday.fixed("Immaculate Conception", Month.DECEMBER, 8);
    public static final Holiday CHRISTMAS_EVE = Holiday.fixed("Christmas Eve", Month.DECEMBER, 24);
    public static final Holiday CHRISTMAS = Holiday.fixed("Christmas Day", Month.DECEMBER, 25);
    public static final Holiday BOXING_DAY = Holiday.fixed("Boxing Day", Month.DECEMBER, 26);
    public static final Holiday NEW_YEARS_EVE = Holiday.fixed("New Year's Eve", Month.DECEMBER, 31);

    private Holidays() {}
}
