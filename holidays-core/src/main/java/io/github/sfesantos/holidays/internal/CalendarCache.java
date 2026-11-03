package io.github.sfesantos.holidays.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

/**
 * Lazily builds and keeps one {@link YearIndex} per year. The last index
 * served is kept in a volatile field so the common case, repeated queries
 * on the same year, costs no boxing and no map lookup.
 */
public final class CalendarCache {

    private final ConcurrentHashMap<Integer, YearIndex> indexes = new ConcurrentHashMap<>();
    private final IntFunction<YearIndex> factory;
    private volatile YearIndex last;

    public CalendarCache(IntFunction<YearIndex> factory) {
        this.factory = factory;
    }

    public YearIndex get(int year) {
        var cached = last;

        if (cached != null && cached.year() == year) {
            return cached;
        }

        var index = indexes.get(year);

        if (index == null) {
            index = indexes.computeIfAbsent(year, factory::apply);
        }

        last = index;

        return index;
    }

    public int size() {
        return indexes.size();
    }
}
