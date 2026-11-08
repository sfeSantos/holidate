package io.github.sfesantos.holidays.benchmarks;

import java.time.Year;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.data.Spain;

/**
 * The cold path: what a query pays the first time it touches a year.
 * Target: under 50 µs for a calendar of about twenty
 * holidays. A fresh calendar per invocation keeps the cache empty; the
 * builder runs outside the measured region.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class IndexBenchmark {

    HolidayCalendar fresh;

    @Setup(Level.Invocation)
    public void freshCalendar() {
        fresh = HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();
    }

    @Benchmark
    public HolidayCalendar buildIndexForOneYear() {
        fresh.warmUp(Year.of(2026), Year.of(2026));

        return fresh;
    }

    @Benchmark
    public HolidayCalendar buildCalendar() {
        return HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();
    }
}
