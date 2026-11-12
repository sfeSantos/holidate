package io.github.sfesantos.holidays.benchmarks;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.data.Brazil;
import io.github.sfesantos.holidays.data.Spain;

/**
 * The hot path: queries on years already indexed. Targets:
 * isHoliday under 20 ns with zero allocation, nextHoliday under 100 ns with
 * at most the Optional, businessDaysBetween over ten years under 5 µs.
 * Run with {@code -prof gc} to see {@code gc.alloc.rate.norm}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class QueryBenchmark {

    HolidayCalendar malaga;
    HolidayCalendar eurBrl;
    LocalDate workday;
    LocalDate holiday;
    LocalDate start;
    LocalDate tenYearsLater;
    YearMonth january;

    @Setup
    public void setUp() {
        malaga = HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();
        var rio = HolidayCalendar.builder()
                .layers(Brazil.NATIONAL, Brazil.RIO_DE_JANEIRO, Brazil.RIO_DE_JANEIRO_CITY)
                .build();
        eurBrl = HolidayCalendar.union(malaga, rio);
        malaga.warmUp(Year.of(2020), Year.of(2040));
        eurBrl.warmUp(Year.of(2020), Year.of(2040));
        workday = LocalDate.of(2026, 12, 9);
        holiday = LocalDate.of(2026, 12, 25);
        start = LocalDate.of(2026, 1, 1);
        tenYearsLater = LocalDate.of(2036, 1, 1);
        january = YearMonth.of(2026, 1);
    }

    @Benchmark
    public boolean isHoliday() {
        return malaga.isHoliday(holiday);
    }

    @Benchmark
    public boolean isHolidayOnAWorkday() {
        return malaga.isHoliday(workday);
    }

    @Benchmark
    public boolean isBusinessDay() {
        return malaga.isBusinessDay(workday);
    }

    @Benchmark
    public Optional<HolidayOccurrence> holidayOn() {
        return malaga.holidayOn(holiday);
    }

    @Benchmark
    public Optional<HolidayOccurrence> nextHoliday() {
        return malaga.nextHoliday(workday);
    }

    @Benchmark
    public LocalDate nextBusinessDay() {
        return malaga.nextBusinessDay(holiday);
    }

    @Benchmark
    public LocalDate addTwoBusinessDays() {
        return malaga.addBusinessDays(workday, 2);
    }

    @Benchmark
    public long businessDaysBetweenTenYears() {
        return malaga.businessDaysBetween(start, tenYearsLater);
    }

    @Benchmark
    public Optional<LocalDate> fifthBusinessDayOfMonth() {
        return malaga.nthBusinessDayOfMonth(january, 5);
    }

    @Benchmark
    public Optional<LocalDate> lastBusinessDayOfMonth() {
        return malaga.lastBusinessDayOfMonth(january);
    }

    @Benchmark
    public int businessDaysInMonth() {
        return malaga.businessDaysInMonth(january);
    }

    @Benchmark
    public List<HolidayOccurrence> holidaysInMonth() {
        return malaga.holidaysInMonth(january);
    }

    @Benchmark
    public List<HolidayOccurrence> holidaysInNextThreeMonths() {
        return malaga.holidaysInNextMonths(workday, 3);
    }

    @Benchmark
    public List<HolidayOccurrence> holidaysInNextTwoYears() {
        return malaga.holidaysInNextYears(workday, 2);
    }

    @Benchmark
    public boolean unionIsBusinessDay() {
        return eurBrl.isBusinessDay(workday);
    }

    @Benchmark
    public LocalDate unionAddTwoBusinessDays() {
        return eurBrl.addBusinessDays(workday, 2);
    }
}
