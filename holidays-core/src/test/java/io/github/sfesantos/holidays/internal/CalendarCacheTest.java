package io.github.sfesantos.holidays.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Month;
import java.util.ArrayList;
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
}
