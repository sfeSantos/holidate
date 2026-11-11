package io.github.sfesantos.holidays.internal;

import java.util.function.IntFunction;

/**
 * Lazily builds and keeps one {@link YearIndex} per year. Lookups take no
 * lock and never box the year: the last index served is kept in a volatile
 * field for repeated queries on the same year, and the rest sit in an
 * open-addressed table probed by {@code year & mask}. Building and growing
 * happen under the monitor, so each year is built once. Entries are never
 * removed, which is what lets readers probe without the lock.
 */
public final class CalendarCache {

    private static final int INITIAL_CAPACITY = 16;

    private final IntFunction<YearIndex> factory;
    private volatile YearIndex[] table = new YearIndex[INITIAL_CAPACITY];
    private volatile YearIndex last;
    private int size;

    public CalendarCache(IntFunction<YearIndex> factory) {
        this.factory = factory;
    }

    public YearIndex get(int year) {
        var cached = last;

        if (cached != null && cached.year() == year) {
            return cached;
        }

        var index = find(table, year);

        if (index == null) {
            index = build(year);
        }

        last = index;

        return index;
    }

    public synchronized int size() {
        return size;
    }

    /** The table is at most half full, so the probe always reaches the year or an empty slot. */
    private static YearIndex find(YearIndex[] table, int year) {
        int mask = table.length - 1;
        int slot = year & mask;
        var index = table[slot];

        while (index != null && index.year() != year) {
            slot = (slot + 1) & mask;
            index = table[slot];
        }

        return index;
    }

    private synchronized YearIndex build(int year) {
        var index = find(table, year);

        if (index != null) {
            return index;
        }

        index = factory.apply(year);
        var target = table;

        if ((size + 1) * 2 > target.length) {
            target = grown(target);
        }

        insert(target, index);
        size++;
        table = target;

        return index;
    }

    private static YearIndex[] grown(YearIndex[] table) {
        var larger = new YearIndex[table.length * 2];

        for (var index : table) {
            if (index != null) {
                insert(larger, index);
            }
        }

        return larger;
    }

    private static void insert(YearIndex[] table, YearIndex index) {
        int mask = table.length - 1;
        int slot = index.year() & mask;

        while (table[slot] != null) {
            slot = (slot + 1) & mask;
        }

        table[slot] = index;
    }
}
