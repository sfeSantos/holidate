package io.github.sfesantos.holidays.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Layer;

class CalendarCacheTest {

    private static CompiledCalendar calendar() {
        return assertInstanceOf(CompiledCalendar.class, HolidayCalendar.builder()
                .layer(Layer.named("test").add(Holiday.fixed("Christmas", Month.DECEMBER, 25)).build())
                .build());
    }

    @Test
    void buildsEachYearOnceAndReturnsTheSameInstance() {
        var builds = new AtomicInteger();
        var builder = new YearIndexBuilder(calendar());
        var cache = new CalendarCache(year -> {
            builds.incrementAndGet();

            return builder.build(year);
        });

        var first = cache.get(2026);

        assertSame(first, cache.get(2026));
        assertSame(first, cache.get(2026));
        cache.get(2027);
        assertSame(first, cache.get(2026));
        assertEquals(2, builds.get());
        assertEquals(2, cache.size());
    }

    @Test
    void concurrentQueriesBuildEachYearExactlyOnce() throws Exception {
        var builds = new AtomicInteger();
        var builder = new YearIndexBuilder(calendar());
        var cache = new CalendarCache(year -> {
            builds.incrementAndGet();

            return builder.build(year);
        });
        var start = new CountDownLatch(1);
        var results = new ArrayList<Future<YearIndex>>();

        try (var pool = Executors.newFixedThreadPool(100)) {
            for (int thread = 0; thread < 100; thread++) {
                int year = 2020 + thread % 10;
                results.add(pool.submit(() -> {
                    start.await();

                    return cache.get(year);
                }));
            }

            start.countDown();

            for (var result : results) {
                assertEquals(2020 + results.indexOf(result) % 10, result.get().year());
            }
        }

        assertEquals(10, builds.get());
        assertEquals(10, cache.size());
    }

    @Test
    void keepsEveryYearAsTheTableGrows() {
        var builds = new AtomicInteger();
        var builder = new YearIndexBuilder(calendar());
        var cache = new CalendarCache(year -> {
            builds.incrementAndGet();

            return builder.build(year);
        });
        var built = new HashMap<Integer, YearIndex>();

        for (int year = 1800; year < 2300; year++) {
            built.put(year, cache.get(year));
        }

        for (int year = 2299; year >= 1800; year--) {
            assertEquals(year, cache.get(year).year());
            assertSame(built.get(year), cache.get(year));
        }

        assertEquals(500, builds.get());
        assertEquals(500, cache.size());
    }

    @Test
    void yearsThatShareASlotStayDistinct() {
        var builder = new YearIndexBuilder(calendar());
        var cache = new CalendarCache(builder::build);
        int[] years = {2026, 2042, 2026 + 1024, 10, -6, Year.MAX_VALUE - 5, Year.MIN_VALUE + 6};

        for (int year : years) {
            assertEquals(year, cache.get(year).year());
        }

        for (int year : years) {
            assertEquals(year, cache.get(year).year());
        }

        assertEquals(years.length, cache.size());
    }

    @Test
    void readersSeeTheRightYearWhileTheTableGrows() throws Exception {
        var builds = new AtomicInteger();
        var builder = new YearIndexBuilder(calendar());
        var cache = new CalendarCache(year -> {
            builds.incrementAndGet();

            return builder.build(year);
        });
        var start = new CountDownLatch(1);
        var results = new ArrayList<Future<Boolean>>();

        for (int year = 2000; year < 2008; year++) {
            cache.get(year);
        }

        try (var pool = Executors.newFixedThreadPool(16)) {
            for (int thread = 0; thread < 16; thread++) {
                int offset = thread;
                results.add(pool.submit(() -> {
                    boolean right = true;
                    start.await();

                    for (int i = 0; i < 20_000; i++) {
                        int year = offset % 2 == 0 ? 2000 + i % 8 : 2008 + (i * 7 + offset) % 400;
                        right &= cache.get(year).year() == year;
                    }

                    return right;
                }));
            }

            start.countDown();

            for (var result : results) {
                assertTrue(result.get());
            }
        }

        assertEquals(408, builds.get());
        assertEquals(408, cache.size());
    }
}
