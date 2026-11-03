/**
 * Rule engine for layered holiday calendars and business day queries.
 *
 * <p>Depends on {@code java.base} only. Exports the public API packages;
 * everything under {@code internal} stays encapsulated.
 */
module io.github.sfesantos.holidays {
    exports io.github.sfesantos.holidays;
    exports io.github.sfesantos.holidays.easter;
}
