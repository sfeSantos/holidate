package io.github.sfesantos.holidays.benchmarks;

import java.time.Year;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.data.Spain;

/**
 * Not a JMH benchmark: prints the heap a calendar holds after indexing one
 * hundred years. Target: under 500 KB. Run with a serial
 * collector and a small heap so the number is stable:
 * {@code java -XX:+UseSerialGC -Xmx32m -cp target/benchmarks.jar
 * io.github.sfesantos.holidays.benchmarks.MemoryFootprint}.
 */
public final class MemoryFootprint {

    private static final int YEARS = 100;

    private MemoryFootprint() {}

    public static void main(String[] args) {
        var calendar = HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();
        long before = usedHeap();

        calendar.warmUp(Year.of(1950), Year.of(1950 + YEARS - 1));
        long after = usedHeap();

        System.out.printf("%d years indexed: %,d bytes (%,d per year)%n", YEARS, after - before, (after - before) / YEARS);
        System.out.println(calendar.isHoliday(Year.of(2026).atDay(359)) ? "" : "unreachable");
    }

    private static long usedHeap() {
        var runtime = Runtime.getRuntime();

        for (int i = 0; i < 3; i++) {
            System.gc();
        }

        return runtime.totalMemory() - runtime.freeMemory();
    }
}
