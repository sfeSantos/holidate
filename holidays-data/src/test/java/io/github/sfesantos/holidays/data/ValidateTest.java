package io.github.sfesantos.holidays.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ValidateTest {

    private static final String CHILD = """
            { "layers": [ { "id": "ES-AN-XX", "extends": "ES-AN",
                "holidays": [ { "name": "Feria", "rule": { "fixed": "06-15" } } ] } ] }
            """;

    private static final String OWN_SPAIN = """
            { "layers": [ { "id": "ES",
                "holidays": [ { "name": "Solo", "rule": { "fixed": "03-10" } } ] } ] }
            """;

    private static final String WRONG_KIND = """
            { "layers": [ { "id": "XX",
                "holidays": [
                  { "name": "Typo", "rule": { "fixd": "01-01" } } ] } ] }
            """;

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private Path spain;

    @BeforeEach
    void copyTheShippedPack() throws IOException {
        spain = directory.resolve("ES.json");

        try (var pack = Validate.class.getResourceAsStream("/META-INF/holidays/ES.json")) {
            Files.copy(pack, spain);
        }
    }

    @Test
    void printsTheYearTheOfficialCalendarGives() {
        assertEquals(0, run(spain, "ES-AN-MA", "2026"));

        var lines = lines(out);
        var observed = lines.subList(3, lines.size())
                .stream()
                .map(line -> LocalDate.parse(line.substring(0, 10)))
                .toList();

        assertEquals("ES-AN-MA, 2026: 14 holidays", lines.get(0));
        assertEquals("", lines.get(1));
        assertTrue(lines.get(2).matches("Observed +Day +Name +Type +Layer +Original"), lines.get(2));
        assertEquals(Observed.datesIn("ES-AN-MA", 2026), observed);
        assertEquals("", err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void aMovedHolidayShowsItsOriginalDate() {
        run(spain, "ES-AN-MA", "2026");

        var lines = lines(out);

        assertTrue(lines.contains("2026-11-02  Mon  Todos los Santos"
                + " ".repeat(33) + "PUBLIC  ES        2026-11-01"), String.join("\n", lines));
        assertTrue(lines.stream().anyMatch(line -> line.matches("2026-12-25  Fri  Navidad +PUBLIC  ES")));
    }

    @Test
    void aMonthAndRangesOfYearsOrMonths() {
        assertEquals(0, run(spain, "ES-AN-MA", "2026-12"));
        assertEquals("ES-AN-MA, 2026-12: 3 holidays", lines(out).get(0));

        out.reset();
        assertEquals(0, run(spain, "ES-AN-MA", "2026..2027"));
        assertEquals(3 + 28, lines(out).size());

        out.reset();
        assertEquals(0, run(spain, "ES-AN-MA", "2026-12..2027-01"));

        var lines = lines(out);

        assertEquals(3 + 5, lines.size());
        assertTrue(lines.get(3).startsWith("2026-12-07"));
        assertTrue(lines.get(7).startsWith("2027-01-06"));
    }

    @Test
    void aPeriodWithNothingInItPrintsOnlyTheHeader() {
        assertEquals(0, run(spain, "ES-AN-MA", "2026-07"));
        assertEquals(List.of("ES-AN-MA, 2026-07: 0 holidays", "", "Observed  Day  Name  Type  Layer  Original"), lines(out));
    }

    @Test
    void parentsMissingFromTheFileComeFromTheShippedPacks() throws IOException {
        assertEquals(0, run(write("child.json", CHILD), "ES-AN-XX", "2026-06"));
        assertEquals("2026-06-15  Mon  Feria  PUBLIC  ES-AN-XX", lines(out).get(3));

        out.reset();
        assertEquals(0, run(write("child.json", CHILD), "ES-AN-XX", "2026"));
        assertEquals("ES-AN-XX, 2026: 13 holidays", lines(out).get(0));
    }

    @Test
    void theFileWinsOverTheShippedPack() throws IOException {
        assertEquals(0, run(write("own.json", OWN_SPAIN), "ES", "2026"));
        assertEquals("ES, 2026: 1 holiday", lines(out).get(0));
        assertTrue(lines(out).get(3).startsWith("2026-03-10  Tue  Solo"));
    }

    @Test
    void aWrongRuleKindNamesTheLineTheColumnAndTheValidKinds() throws IOException {
        assertEquals(1, run(write("wrong.json", WRONG_KIND), "XX", "2026"));

        var message = err.toString(StandardCharsets.UTF_8);

        assertTrue(message.contains("wrong.json: line 3, column "), message);
        assertTrue(message.contains("fixed") && message.contains("easter") && message.contains("weekdayAfter"), message);
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void brokenJsonAndMissingFilesFail() throws IOException {
        assertEquals(1, run(write("broken.json", "{ \"layers\": ["), "XX", "2026"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("broken.json: line 1, column "));

        err.reset();
        assertEquals(1, run(directory.resolve("absent.json"), "XX", "2026"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("absent.json: no such file"));
    }

    @Test
    void anUnknownIdListsTheOnesInTheFile() {
        assertEquals(1, run(spain, "ES-XX", "2026"));
        assertTrue(err.toString(StandardCharsets.UTF_8)
                .contains("unknown layer id 'ES-XX'; the file defines [ES, ES-AN, ES-AN-MA, ES-CT]"), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void aParentNobodyDefinesFails() throws IOException {
        var orphan = CHILD.replace("\"ES-AN\"", "\"ZZ\"");

        assertEquals(1, run(write("orphan.json", orphan), "ES-AN-XX", "2026"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("extends 'ZZ' but no provider defines it"));
    }

    @Test
    void badPeriodsAndArgumentCountsPrintTheUsage() {
        for (var period : List.of("26", "2026-13", "2026-1", "next", "2027..2026", "2026-03..2026-02", "2026..", "")) {
            err.reset();

            assertEquals(1, run(spain, "ES-AN-MA", period), period);
            assertTrue(err.toString(StandardCharsets.UTF_8).contains("usage: Validate"), period);
        }

        err.reset();
        assertEquals(1, Validate.run(new String[] {"ES.json", "ES"}, stream(out), stream(err)));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("usage: Validate <file.json> <id> <period>"));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("null"));
    }

    private int run(Path file, String id, String period) {
        return Validate.run(new String[] {file.toString(), id, period}, stream(out), stream(err));
    }

    private Path write(String name, String json) throws IOException {
        return Files.writeString(directory.resolve(name), json);
    }

    private static PrintStream stream(ByteArrayOutputStream bytes) {
        return new PrintStream(bytes, true, StandardCharsets.UTF_8);
    }

    private static List<String> lines(ByteArrayOutputStream bytes) {
        return bytes.toString(StandardCharsets.UTF_8).lines().toList();
    }
}
