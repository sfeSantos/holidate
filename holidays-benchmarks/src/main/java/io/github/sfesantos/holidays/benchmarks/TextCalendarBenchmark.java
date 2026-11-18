package io.github.sfesantos.holidays.benchmarks;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Locale;
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
import io.github.sfesantos.holidays.data.Spain;
import io.github.sfesantos.holidays.format.TextCalendar;

/**
 * Printing the text calendar from an instance configured once, which is the
 * intended use, and from scratch. A print should allocate the builder's
 * array and the returned String and nothing else; run with {@code -prof gc}
 * to see it. With holidays it also pays for the list the calendar returns.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class TextCalendarBenchmark {

    Clock clock;
    TextCalendar today;
    TextCalendar month;
    TextCalendar monthWithDay;
    TextCalendar monthWithHolidays;
    TextCalendar year;
    TextCalendar yearWithHolidays;
    StringBuilder reused;

    @Setup
    public void setUp() {
        var malaga = HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();
        malaga.warmUp(Year.of(2020), Year.of(2040));
        clock = Clock.fixed(Instant.parse("2026-12-18T10:15:30Z"), ZoneOffset.UTC);
        today = TextCalendar.of(clock);
        month = today.year(2026).month(12);
        monthWithDay = month.day(18);
        monthWithHolidays = month.holidays(malaga);
        year = today.year(2026);
        yearWithHolidays = year.holidays(malaga);
        reused = new StringBuilder(256);
    }

    @Benchmark
    public String month() {
        return month.print();
    }

    @Benchmark
    public String monthWithDay() {
        return monthWithDay.print();
    }

    @Benchmark
    public String monthWithHolidays() {
        return monthWithHolidays.print();
    }

    /** Nothing given: the clock is read for the month and for the day to mark. */
    @Benchmark
    public String today() {
        return today.print();
    }

    @Benchmark
    public StringBuilder monthIntoAReusedBuilder() {
        reused.setLength(0);
        month.printTo(reused);

        return reused;
    }

    @Benchmark
    public String year() {
        return year.print();
    }

    @Benchmark
    public String yearWithHolidays() {
        return yearWithHolidays.print();
    }

    @Benchmark
    public String configureAndPrintMonth() {
        return TextCalendar.of(clock).year(2026).month(12).day(18).print();
    }

    /** What an instance pays once for names in another language. */
    @Benchmark
    public TextCalendar configureLocale() {
        return today.locale(Locale.GERMAN);
    }
}
