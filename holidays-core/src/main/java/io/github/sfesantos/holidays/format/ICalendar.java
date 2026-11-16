package io.github.sfesantos.holidays.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;

/**
 * Writes a calendar's holidays as iCalendar (RFC 5545) text, one all-day
 * {@code VEVENT} per occurrence on its observed date. Files are UTF-8. A
 * calendar has no name of its own, so the caller passes one, such as
 * {@code "ES-AN-MA"}; it names the calendar in the client and is part of
 * every {@code UID}. The {@code UID} depends only on that name, the layer,
 * the holiday and the observed date, so importing a later export updates
 * the events instead of duplicating them. The clock only stamps
 * {@code DTSTAMP}.
 *
 * <p>Years are listed with {@code holidaysIn(Year)}, which goes by the year
 * of the rule: a moved or shifted date can fall in a neighbouring year.
 */
public final class ICalendar {

    private static final String CRLF = "\r\n";
    private static final int MAX_LINE_OCTETS = 75;
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private ICalendar() {}

    /** Every holiday from {@code from} to {@code until}, both inclusive. */
    public static String write(HolidayCalendar calendar, String name, Year from, Year until, Clock clock) {
        Objects.requireNonNull(calendar, "calendar must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(until, "until must not be null");
        Objects.requireNonNull(clock, "clock must not be null");

        if (until.isBefore(from)) {
            throw new IllegalArgumentException("until must not be before from");
        }

        var stamp = STAMP.format(clock.instant());
        var out = new StringBuilder();
        line(out, "BEGIN:VCALENDAR");
        line(out, "VERSION:2.0");
        line(out, "PRODID:-//sfesantos//holidate//EN");
        line(out, "CALSCALE:GREGORIAN");
        line(out, "X-WR-CALNAME:" + escape(name));

        for (var year = from.getValue(); year <= until.getValue(); year++) {
            for (var occurrence : calendar.holidaysIn(Year.of(year))) {
                event(out, name, occurrence, stamp);
            }
        }

        line(out, "END:VCALENDAR");

        return out.toString();
    }

    public static void write(HolidayCalendar calendar, String name, Year from, Year until, Clock clock, Writer writer) {
        Objects.requireNonNull(writer, "writer must not be null");

        try {
            writer.write(write(calendar, name, from, until, clock));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(HolidayCalendar calendar, String name, Year from, Year until, Clock clock, Path file) {
        Objects.requireNonNull(file, "file must not be null");

        try {
            Files.writeString(file, write(calendar, name, from, until, clock), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void event(StringBuilder out, String name, HolidayOccurrence occurrence, String stamp) {
        var observed = occurrence.observed();
        var start = DateTimeFormatter.BASIC_ISO_DATE.format(observed);
        var description = "Type: " + occurrence.type().name() + "\nLayer: " + occurrence.layer();

        if (occurrence.isMoved()) {
            description += "\nOriginal date: " + occurrence.original();
        }

        line(out, "BEGIN:VEVENT");
        line(out, "UID:" + token(name) + "-" + token(occurrence.layer()) + "-" + token(occurrence.name()) + "-" + start
                + "@holidate");
        line(out, "DTSTAMP:" + stamp);
        line(out, "DTSTART;VALUE=DATE:" + start);
        line(out, "DTEND;VALUE=DATE:" + DateTimeFormatter.BASIC_ISO_DATE.format(observed.plusDays(1)));
        line(out, "SUMMARY:" + escape(occurrence.name()));
        line(out, "CATEGORIES:" + occurrence.type().name());
        line(out, "DESCRIPTION:" + escape(description));
        line(out, "TRANSP:TRANSPARENT");
        line(out, "END:VEVENT");
    }

    private static String escape(String text) {
        var escaped = text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,");

        return escaped.replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n");
    }

    /** A UID part: letters and digits kept, every run of anything else becomes one hyphen. */
    private static String token(String text) {
        var out = new StringBuilder();
        var pendingHyphen = false;

        for (var index = 0; index < text.length(); ) {
            var codePoint = text.codePointAt(index);
            index += Character.charCount(codePoint);

            if (!Character.isLetterOrDigit(codePoint)) {
                pendingHyphen = !out.isEmpty();
                continue;
            }

            if (pendingHyphen) {
                out.append('-');
                pendingHyphen = false;
            }

            out.appendCodePoint(codePoint);
        }

        return out.toString();
    }

    /**
     * Appends one content line, folded so that no physical line passes 75
     * octets of UTF-8. A fold never lands inside a character, and the space
     * that opens a continuation counts toward that line.
     */
    private static void line(StringBuilder out, String content) {
        var octets = 0;

        for (var index = 0; index < content.length(); ) {
            var codePoint = content.codePointAt(index);
            var width = utf8Width(codePoint);

            if (octets + width > MAX_LINE_OCTETS) {
                out.append(CRLF).append(' ');
                octets = 1;
            }

            out.appendCodePoint(codePoint);
            octets += width;
            index += Character.charCount(codePoint);
        }

        out.append(CRLF);
    }

    private static int utf8Width(int codePoint) {
        if (codePoint < 0x80) {
            return 1;
        }

        if (codePoint < 0x800) {
            return 2;
        }

        return codePoint < 0x10000 ? 3 : 4;
    }
}
