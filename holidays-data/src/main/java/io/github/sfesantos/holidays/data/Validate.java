package io.github.sfesantos.holidays.data;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.format.HolidayFormat;
import io.github.sfesantos.holidays.registry.CalendarRegistry;
import io.github.sfesantos.holidays.registry.HolidayDataProvider;

/**
 * Checks a pack file and prints the holidays a layer of it observes in a
 * period, so a pack can be reviewed without writing Java:
 * {@code java -cp holidays-core.jar:holidays-data.jar
 * io.github.sfesantos.holidays.data.Validate ES.json ES-AN-MA 2027}.
 * Parents the file does not define come from the shipped packs. The period is
 * a year, a month ({@code 2027-03}) or a range of either with both ends
 * included ({@code 2026..2028}, {@code 2026-11..2027-02}). Exits with 1 on
 * any error.
 */
public final class Validate {

    private static final String USAGE = """
            usage: Validate <file.json> <id> <period>
              period: a year (2027), a month (2027-03), or a range with both ends
                      included (2026..2028, 2026-11..2027-02)""";
    private static final String[] HEADER = {"Observed", "Day", "Name", "Type", "Layer", "Original"};
    private static final Pattern YEAR = Pattern.compile("\\d{4}");
    private static final Pattern MONTH = Pattern.compile("\\d{4}-\\d{2}");

    private Validate() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 3) {
            err.println(USAGE);

            return 1;
        }

        Span span;

        try {
            span = Span.parse(args[2]);
        } catch (RuntimeException e) {
            err.println(e.getMessage());
            err.println(USAGE);

            return 1;
        }

        try {
            var file = Path.of(args[0]);

            if (!Files.isRegularFile(file)) {
                err.println(args[0] + ": no such file");

                return 1;
            }

            var layers = HolidayDataProvider.of(HolidayFormat.json().parse(file));
            var registry = CalendarRegistry.of(layers, new BundledDataProvider());

            if (registry.layer(args[1]).isEmpty()) {
                err.println(args[0] + ": unknown layer id '" + args[1] + "'; the file defines " + new TreeSet<>(layers.layerIds()));

                return 1;
            }

            var holidays = registry.calendar(args[1]).holidaysBetween(span.from(), span.to());
            print(out, args[1] + ", " + args[2], holidays);

            return 0;
        } catch (UncheckedIOException e) {
            err.println(args[0] + ": " + e.getCause());
        } catch (RuntimeException e) {
            err.println(args[0] + ": " + e.getMessage());
        }

        return 1;
    }

    private static void print(PrintStream out, String title, List<HolidayOccurrence> holidays) {
        var rows = new ArrayList<String[]>();
        rows.add(HEADER);

        for (var holiday : holidays) {
            rows.add(new String[] {
                    holiday.observed().toString(),
                    holiday.observed().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ROOT),
                    holiday.name(),
                    holiday.type().name(),
                    holiday.layer(),
                    holiday.isMoved() ? holiday.original().toString() : ""});
        }

        var widths = new int[HEADER.length];

        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        out.println(title + ": " + holidays.size() + (holidays.size() == 1 ? " holiday" : " holidays"));
        out.println();

        for (var row : rows) {
            var line = new StringBuilder();

            for (int i = 0; i < row.length; i++) {
                line.append(row[i]).append(" ".repeat(widths[i] - row[i].length() + 2));
            }

            out.println(line.toString().stripTrailing());
        }
    }

    /** {@code from} inclusive, {@code to} exclusive. */
    private record Span(LocalDate from, LocalDate to) {

        static Span parse(String period) {
            int dots = period.indexOf("..");
            var first = dots < 0 ? period : period.substring(0, dots);
            var last = dots < 0 ? period : period.substring(dots + 2);
            var span = new Span(start(first), end(last));

            if (!span.from.isBefore(span.to)) {
                throw new IllegalArgumentException("period '" + period + "' ends before it starts");
            }

            return span;
        }

        private static LocalDate start(String text) {
            if (YEAR.matcher(text).matches()) {
                return Year.parse(text).atDay(1);
            }

            return month(text).atDay(1);
        }

        private static LocalDate end(String text) {
            if (YEAR.matcher(text).matches()) {
                return Year.parse(text).plusYears(1).atDay(1);
            }

            return month(text).plusMonths(1).atDay(1);
        }

        private static YearMonth month(String text) {
            if (!MONTH.matcher(text).matches()) {
                throw new IllegalArgumentException("'" + text + "' is not a year or a month");
            }

            try {
                return YearMonth.parse(text);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("'" + text + "' is not a year or a month", e);
            }
        }
    }
}
