package io.github.sfesantos.holidays.benchmarks;

import java.time.LocalDate;
import java.time.Month;
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

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.easter.EasterAlgorithm;

/**
 * The Easter computations on their own, and what an Orthodox calendar pays
 * the first time it touches a year. Easter is computed while a year's index
 * is built, never on the query path.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class EasterBenchmark {

    static final Layer GREECE = Layer.named("GR")
            .add(Holiday.fixed("Πρωτοχρονιά", Month.JANUARY, 1),
                    Holiday.fixed("Θεοφάνια", Month.JANUARY, 6),
                    Holiday.easterRelative("Καθαρά Δευτέρα", -48),
                    Holiday.fixed("Ευαγγελισμός της Θεοτόκου", Month.MARCH, 25),
                    Holiday.easterRelative("Μεγάλη Παρασκευή", -2),
                    Holiday.easterRelative("Δευτέρα του Πάσχα", 1),
                    Holiday.fixed("Εργατική Πρωτομαγιά", Month.MAY, 1),
                    Holiday.easterRelative("Αγίου Πνεύματος", 50),
                    Holiday.fixed("Κοίμηση της Θεοτόκου", Month.AUGUST, 15),
                    Holiday.fixed("Επέτειος του Όχι", Month.OCTOBER, 28),
                    Holiday.fixed("Χριστούγεννα", Month.DECEMBER, 25),
                    Holiday.fixed("Σύναξη της Θεοτόκου", Month.DECEMBER, 26))
            .build();

    int year = 2026;

    /** Kept apart so that only the index benchmark pays for a calendar per invocation. */
    @State(Scope.Thread)
    public static class FreshCalendar {

        HolidayCalendar calendar;

        @Setup(Level.Invocation)
        public void build() {
            calendar = HolidayCalendar.builder()
                    .easter(EasterAlgorithm.orthodox())
                    .layer(GREECE)
                    .build();
        }
    }

    @Benchmark
    public LocalDate gregorian() {
        return EasterAlgorithm.gregorian().easterSunday(year);
    }

    @Benchmark
    public LocalDate julian() {
        return EasterAlgorithm.julian().easterSunday(year);
    }

    @Benchmark
    public LocalDate orthodox() {
        return EasterAlgorithm.orthodox().easterSunday(year);
    }

    @Benchmark
    public HolidayCalendar buildIndexForOneOrthodoxYear(FreshCalendar fresh) {
        fresh.calendar.warmUp(Year.of(2026), Year.of(2026));

        return fresh.calendar;
    }
}
