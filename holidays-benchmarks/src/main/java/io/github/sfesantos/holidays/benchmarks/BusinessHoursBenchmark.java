package io.github.sfesantos.holidays.benchmarks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.data.Spain;

/**
 * Business-hours arithmetic over 1, 10, 100 and 1000 business days of 8.5 hours.
 * The cost should grow with the days crossed and {@code -prof gc} should
 * show only the result allocated, whatever the distance.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class BusinessHoursBenchmark {

    @Param({"1", "10", "100", "1000"})
    int days;

    HolidayCalendar malaga;
    LocalDateTime start;
    LocalDateTime end;
    Duration duration;
    Duration back;

    @Setup
    public void setUp() {
        malaga = HolidayCalendar.builder()
                .businessHours(LocalTime.of(9, 0), LocalTime.of(17, 30))
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA,
                        Layer.named("office")
                                .add(Holiday.fixed("Nochebuena", Month.DECEMBER, 24).closingAt(LocalTime.of(13, 0)))
                                .build())
                .build();
        malaga.warmUp(Year.of(2025), Year.of(2032));
        start = LocalDateTime.of(2026, 9, 1, 10, 0);
        duration = Duration.ofMinutes(510L * days);
        back = duration.negated();
        end = malaga.addBusinessHours(start, duration);
    }

    @Benchmark
    public LocalDateTime addBusinessHours() {
        return malaga.addBusinessHours(start, duration);
    }

    @Benchmark
    public LocalDateTime subtractBusinessHours() {
        return malaga.addBusinessHours(end, back);
    }

    @Benchmark
    public Duration businessDurationBetween() {
        return malaga.businessDurationBetween(start, end);
    }
}
