package io.github.sfesantos.holidays.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.sfesantos.holidays.HolidayCalendar;

/**
 * Prints a month or a whole year as a text grid, with one day optionally
 * marked in brackets. Year, month and day are each optional:
 *
 * <ul>
 * <li>a year alone prints its twelve months, three to a row;</li>
 * <li>anything else prints one month. A missing year is the clock's year and
 * a missing month is the clock's month;</li>
 * <li>with a day given, that day is marked. With nothing given at all, the
 * clock's month is printed with today marked.</li>
 * </ul>
 *
 * <p>Weeks start on Monday and names are English unless
 * {@link #firstDayOfWeek(DayOfWeek)} or {@link #locale(Locale)} says
 * otherwise; the JVM's default locale is never read. Every line ends in
 * {@code \n} and none has trailing spaces. When the marked day sits in the
 * first column the whole month moves one column to the right to make room for
 * the opening bracket.
 *
 * <p>With {@link #holidays(HolidayCalendar)} every holiday gets a {@code *}
 * after its day, on the date it is observed. The mark and the brackets share
 * the one column between two days, and the brackets win: a holiday that is
 * the marked day, or the day right before it, shows no {@code *}.
 *
 * <p>Instances are immutable: every method returns a new one, so a configured
 * instance can be kept and shared between threads. Names are resolved when the
 * locale or the first day of the week is set, not when printing.
 */
public final class TextCalendar {

    private static final int ABSENT = Integer.MIN_VALUE;
    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS = 6;
    private static final int CELL = 3;
    private static final int MONTH_WIDTH = 20;
    private static final int MONTH_GAP = 2;
    private static final int MONTHS_PER_ROW = 3;
    private static final int YEAR_WIDTH = MONTHS_PER_ROW * MONTH_WIDTH + (MONTHS_PER_ROW - 1) * MONTH_GAP;
    private static final int MONTH_CAPACITY = (MAX_WEEKS + 2) * (MONTH_WIDTH + 3);
    private static final int YEAR_CAPACITY = 2 * (YEAR_WIDTH + 2)
            + (12 / MONTHS_PER_ROW) * ((MAX_WEEKS + 2) * (YEAR_WIDTH + 2) + 1);
    private static final int[] DAYS_BEFORE_MONTH = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
    private static final int[] NO_MARKS = new int[12];
    private static final String BLANK = " ".repeat(YEAR_WIDTH);
    /** Every day right-aligned in two columns with a space after it, so a week is one slice. */
    private static final String DAYS = IntStream.rangeClosed(1, 31)
            .mapToObj(number -> (number < 10 ? " " : "") + number + " ")
            .collect(Collectors.joining());
    private static final Names DEFAULT_NAMES = Names.of(Locale.ENGLISH, DayOfWeek.MONDAY);

    private final Clock clock;
    private final int year;
    private final int month;
    private final int day;
    private final DayOfWeek firstDayOfWeek;
    private final Locale locale;
    private final Names names;
    private final HolidayCalendar holidays;

    private TextCalendar(Clock clock, int year, int month, int day, DayOfWeek firstDayOfWeek, Locale locale,
            Names names, HolidayCalendar holidays) {
        this.clock = clock;
        this.year = year;
        this.month = month;
        this.day = day;
        this.firstDayOfWeek = firstDayOfWeek;
        this.locale = locale;
        this.names = names;
        this.holidays = holidays;
    }

    /** The clock supplies whatever part of the date is not given; it is read only when one is missing. */
    public static TextCalendar of(Clock clock) {
        Objects.requireNonNull(clock, "clock must not be null");

        return new TextCalendar(clock, ABSENT, ABSENT, ABSENT, DayOfWeek.MONDAY, Locale.ENGLISH, DEFAULT_NAMES, null);
    }

    public TextCalendar year(int year) {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "year must be between " + Year.MIN_VALUE + " and " + Year.MAX_VALUE + ": " + year);
        }

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, names, holidays);
    }

    public TextCalendar month(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12: " + month);
        }

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, names, holidays);
    }

    /** A day the printed month does not have, such as 31 in April, is rejected when printing. */
    public TextCalendar day(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31: " + day);
        }

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, names, holidays);
    }

    public TextCalendar firstDayOfWeek(DayOfWeek firstDayOfWeek) {
        Objects.requireNonNull(firstDayOfWeek, "firstDayOfWeek must not be null");

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, Names.of(locale, firstDayOfWeek),
                holidays);
    }

    /** Month and weekday names only; the first day of the week does not follow the locale. */
    public TextCalendar locale(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, Names.of(locale, firstDayOfWeek),
                holidays);
    }

    /** Marks the days the calendar observes a holiday on, of any type. */
    public TextCalendar holidays(HolidayCalendar holidays) {
        Objects.requireNonNull(holidays, "holidays must not be null");

        return new TextCalendar(clock, year, month, day, firstDayOfWeek, locale, names, holidays);
    }

    public String print() {
        var out = new StringBuilder(isWholeYear() ? YEAR_CAPACITY : MONTH_CAPACITY);
        render(out);

        return out.toString();
    }

    /** A {@code StringBuilder} is written to directly, so one that is reused takes the text with no allocation. */
    public void printTo(Appendable out) {
        Objects.requireNonNull(out, "out must not be null");

        if (out instanceof StringBuilder builder) {
            render(builder);
            return;
        }

        try {
            out.append(print());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isWholeYear() {
        return year != ABSENT && month == ABSENT && day == ABSENT;
    }

    private void render(StringBuilder out) {
        if (isWholeYear()) {
            wholeYear(out);
            return;
        }

        var yearValue = year;
        var monthValue = month;
        var selected = day;

        if (year == ABSENT || month == ABSENT) {
            var today = LocalDate.now(clock);

            if (year == ABSENT && month == ABSENT && day == ABSENT) {
                selected = today.getDayOfMonth();
            }

            yearValue = year == ABSENT ? today.getYear() : year;
            monthValue = month == ABSENT ? today.getMonthValue() : month;
        }

        month(out, yearValue, monthValue, selected);
    }

    private void month(StringBuilder out, int yearValue, int monthValue, int selected) {
        var length = lengthOf(monthValue, Year.isLeap(yearValue));

        if (selected > length) {
            throw new IllegalArgumentException(
                    "day does not exist in " + YearMonth.of(yearValue, monthValue) + ": " + selected);
        }

        var offset = offsetOf(yearValue, monthValue);
        var marks = marksOf(yearValue, monthValue);
        var indent = selected != ABSENT && (offset + selected - 1) % DAYS_IN_WEEK == 0 ? 1 : 0;
        var name = names.months()[monthValue - 1];
        pad(out, indent + Math.max(0, (MONTH_WIDTH - name.length() - 1 - widthOf(yearValue)) / 2));
        out.append(name).append(' ').append(yearValue).append('\n');
        pad(out, indent);
        out.append(names.weekdays()).append('\n');

        for (var firstDay = 1 - offset; firstDay <= length; firstDay += DAYS_IN_WEEK) {
            week(out, out.length() + indent, firstDay, length, selected, marks);
            out.append('\n');
        }
    }

    private void wholeYear(StringBuilder out) {
        var leap = Year.isLeap(year);
        var januaryOffset = offsetOf(year, 1);
        var marks = marksOfYear();
        pad(out, Math.max(0, (YEAR_WIDTH - widthOf(year)) / 2));
        out.append(year).append('\n');

        for (var firstMonth = 1; firstMonth <= 12; firstMonth += MONTHS_PER_ROW) {
            out.append('\n');
            var titleStart = out.length();

            for (var column = 0; column < MONTHS_PER_ROW; column++) {
                var name = names.months()[firstMonth + column - 1];
                var target = column * (MONTH_WIDTH + MONTH_GAP) + Math.max(0, (MONTH_WIDTH - name.length()) / 2);
                pad(out, titleStart + target - out.length());
                out.append(name);
            }

            out.append('\n').append(names.weekdaysOfYear()).append('\n');

            for (var row = 0; row < MAX_WEEKS; row++) {
                var lineStart = out.length();

                for (var column = 0; column < MONTHS_PER_ROW; column++) {
                    var monthValue = firstMonth + column;
                    var offset = (januaryOffset + daysBefore(monthValue, leap)) % DAYS_IN_WEEK;
                    week(out, lineStart + column * (MONTH_WIDTH + MONTH_GAP), row * DAYS_IN_WEEK + 1 - offset,
                            lengthOf(monthValue, leap), ABSENT, marks[monthValue - 1]);
                }

                if (out.length() > lineStart) {
                    out.append('\n');
                }
            }
        }
    }

    /**
     * Appends the days of one week of a month, the week's first column being
     * at {@code start}. {@code firstDay} is the day that column stands for,
     * below 1 when the month starts later in the week. Nothing is written for
     * a week the month does not reach, and nothing after the last day but its
     * mark, so a line never ends in spaces.
     */
    private static void week(StringBuilder out, int start, int firstDay, int length, int selected, int marks) {
        var from = Math.max(firstDay, 1);
        var until = Math.min(firstDay + DAYS_IN_WEEK - 1, length);

        if (from > until) {
            return;
        }

        pad(out, start + (from - firstDay) * CELL - out.length());
        out.append(DAYS, (from - 1) * CELL, until * CELL - 1);

        for (var rest = marks & (-1 << from) & ~(-2 << until); rest != 0; rest &= rest - 1) {
            mark(out, start + (Integer.numberOfTrailingZeros(rest) - firstDay) * CELL + CELL - 1, '*');
        }

        if (selected >= from && selected <= until) {
            var cell = start + (selected - firstDay) * CELL;
            mark(out, cell - 1, '[');
            mark(out, cell + CELL - 1, ']');
        }
    }

    /** Writes over the column between two days, or appends when the day is the last of its line. */
    private static void mark(StringBuilder out, int position, char sign) {
        if (position < out.length()) {
            out.setCharAt(position, sign);
        } else {
            out.append(sign);
        }
    }

    /** Bit {@code n} is set when day {@code n} of the month is a holiday. */
    private int marksOf(int yearValue, int monthValue) {
        if (holidays == null) {
            return 0;
        }

        var marks = 0;

        for (var occurrence : holidays.holidaysInMonth(YearMonth.of(yearValue, monthValue))) {
            marks |= 1 << occurrence.observed().getDayOfMonth();
        }

        return marks;
    }

    private int[] marksOfYear() {
        if (holidays == null) {
            return NO_MARKS;
        }

        var first = LocalDate.of(year, 1, 1);
        var marks = new int[12];

        for (var occurrence : holidays.holidaysBetween(first, first.plusYears(1))) {
            var observed = occurrence.observed();
            marks[observed.getMonthValue() - 1] |= 1 << observed.getDayOfMonth();
        }

        return marks;
    }

    /** How many columns the first day of the month sits from the first day of the week. */
    private int offsetOf(int yearValue, int monthValue) {
        var weekday = LocalDate.of(yearValue, monthValue, 1).getDayOfWeek();

        return Math.floorMod(weekday.getValue() - firstDayOfWeek.getValue(), DAYS_IN_WEEK);
    }

    private static int daysBefore(int monthValue, boolean leap) {
        return DAYS_BEFORE_MONTH[monthValue - 1] + (leap && monthValue > 2 ? 1 : 0);
    }

    private static int lengthOf(int monthValue, boolean leap) {
        return Month.of(monthValue).length(leap);
    }

    private static int widthOf(int number) {
        var width = number < 0 ? 2 : 1;

        for (var rest = number / 10; rest != 0; rest /= 10) {
            width++;
        }

        return width;
    }

    private static void pad(StringBuilder out, int count) {
        for (var left = count; left > 0; left -= BLANK.length()) {
            out.append(BLANK, 0, Math.min(left, BLANK.length()));
        }
    }

    private record Names(String[] months, String weekdays, String weekdaysOfYear) {

        static Names of(Locale locale, DayOfWeek firstDayOfWeek) {
            var months = new String[12];

            for (var month : Month.values()) {
                months[month.ordinal()] = month.getDisplayName(TextStyle.FULL_STANDALONE, locale);
            }

            var weekdays = new StringBuilder(MONTH_WIDTH);

            for (var index = 0; index < DAYS_IN_WEEK; index++) {
                var name = firstDayOfWeek.plus(index).getDisplayName(TextStyle.SHORT_STANDALONE, locale);
                var letters = Math.min(2, name.codePointCount(0, name.length()));
                pad(weekdays, index == 0 ? 0 : 1);
                pad(weekdays, 2 - letters);
                weekdays.append(name, 0, name.offsetByCodePoints(0, letters));
            }

            var weekdaysOfYear = String.join(" ".repeat(MONTH_GAP), Collections.nCopies(MONTHS_PER_ROW, weekdays));

            return new Names(months, weekdays.toString(), weekdaysOfYear);
        }
    }
}
