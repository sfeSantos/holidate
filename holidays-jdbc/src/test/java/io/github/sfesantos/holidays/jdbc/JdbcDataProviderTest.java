package io.github.sfesantos.holidays.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.jdbc.JdbcSchema.HolidayTable;
import io.github.sfesantos.holidays.jdbc.JdbcSchema.LayerTable;
import io.github.sfesantos.holidays.registry.CalendarRegistry;

class JdbcDataProviderTest {

    private static final AtomicInteger DATABASES = new AtomicInteger();

    private static final String DEFAULT_SCHEMA = """
            CREATE TABLE holiday_layer (
                id          VARCHAR(64) PRIMARY KEY,
                parent_id   VARCHAR(64),
                weekend     VARCHAR(64),
                observance  VARCHAR(40)
            );
            CREATE TABLE holiday (
                layer_id    VARCHAR(64)  NOT NULL,
                name        VARCHAR(200) NOT NULL,
                action      VARCHAR(8),
                rule        VARCHAR(400),
                type        VARCHAR(16),
                valid_from  INTEGER,
                valid_until INTEGER,
                observance  VARCHAR(40),
                closes_at   VARCHAR(5),
                PRIMARY KEY (layer_id, name)
            );
            """;

    private DataSource dataSource;

    @BeforeEach
    void createDatabase() {
        var url = "jdbc:h2:mem:holidays" + DATABASES.incrementAndGet() + ";DB_CLOSE_DELAY=-1";
        dataSource = (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class<?>[] {DataSource.class}, (proxy, method, args) -> DriverManager.getConnection(url));
        run(DEFAULT_SCHEMA);
    }

    @Test
    void registryResolvesAnExtendsChainReadFromTheTables() {
        run("""
                INSERT INTO holiday_layer (id, parent_id, weekend, observance) VALUES
                    ('ES', NULL, 'SATURDAY,SUNDAY', NULL),
                    ('ES-AN', 'ES', NULL, 'SUNDAY_TO_MONDAY'),
                    ('ES-AN-MA', 'ES-AN', NULL, NULL);
                INSERT INTO holiday (layer_id, name, rule) VALUES
                    ('ES', 'Año Nuevo', '{"fixed": "01-01"}'),
                    ('ES', 'Epifanía del Señor', '{"fixed": "01-06"}'),
                    ('ES', 'Viernes Santo', '{"easter": -2}'),
                    ('ES', 'Fiesta del Trabajo', '{"fixed": "05-01"}'),
                    ('ES', 'Asunción de la Virgen', '{"fixed": "08-15"}'),
                    ('ES', 'Fiesta Nacional de España', '{"fixed": "10-12"}'),
                    ('ES', 'Todos los Santos', '{"fixed": "11-01"}'),
                    ('ES', 'Día de la Constitución', '{"fixed": "12-06"}'),
                    ('ES', 'Inmaculada Concepción', '{"fixed": "12-08"}'),
                    ('ES', 'Navidad', '{"fixed": "12-25"}'),
                    ('ES-AN', 'Día de Andalucía', '{"fixed": "02-28"}'),
                    ('ES-AN', 'Jueves Santo', '{"easter": -3}'),
                    ('ES-AN-MA', 'Incorporación de Málaga a la Corona de Castilla', '{"fixed": "08-19"}'),
                    ('ES-AN-MA', 'Virgen de la Victoria', '{"fixed": "09-08"}');
                """);

        var registry = CalendarRegistry.load(JdbcDataProvider.of(dataSource));
        var malaga = registry.calendar("ES-AN-MA");

        assertEquals(Set.of("ES", "ES-AN", "ES-AN-MA"), registry.layerIds());
        assertEquals(List.of("ES", "ES-AN", "ES-AN-MA"), registry.layers("ES-AN-MA")
                .stream()
                .map(Layer::name)
                .toList());
        assertEquals(List.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 6),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 12, 7),
                LocalDate.of(2026, 12, 8),
                LocalDate.of(2026, 12, 25)), observedIn(malaga, 2026));
        assertEquals(12, registry.calendar("ES-AN").holidaysIn(Year.of(2026)).size());
    }

    @Test
    void readsEveryColumnOfALayerAndAHoliday() {
        run("""
                INSERT INTO holiday_layer (id, parent_id, weekend, observance) VALUES
                    ('AE', NULL, ' FRIDAY , SATURDAY ', 'NEAREST_WEEKDAY'),
                    ('OPEN', NULL, '', NULL),
                    ('PLAIN', NULL, NULL, NULL);
                INSERT INTO holiday (layer_id, name, action, rule, type, valid_from, valid_until, observance, closes_at) VALUES
                    ('AE', 'Early close', 'ADD', '{"fixed": "12-24"}', 'HALF_DAY', NULL, NULL, NULL, '13:00'),
                    ('AE', 'Implied half day', NULL, '{"fixed": "12-31"}', NULL, NULL, NULL, NULL, '14:30'),
                    ('AE', 'Bank day', NULL, '{"fixed": "07-01"}', 'BANK', 2000, 2030, 'NONE', NULL),
                    ('AE', 'Day after', NULL, '{"nth": 4, "weekday": "THURSDAY", "month": "NOVEMBER", "shift": 1}', NULL, NULL, NULL, 'INHERIT', NULL);
                """);

        var provider = JdbcDataProvider.of(dataSource);
        var emirates = provider.layer("AE").orElseThrow();
        var byName = emirates.additions()
                .stream()
                .collect(Collectors.toMap(Holiday::name, Function.identity()));

        assertEquals(Optional.of(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)), emirates.weekend());
        assertEquals(ObservanceRule.nearestWeekday(), emirates.observance());
        assertEquals(Optional.of(Weekend.none()), provider.layer("OPEN").orElseThrow().weekend());
        assertEquals(Optional.empty(), provider.layer("PLAIN").orElseThrow().weekend());
        assertFalse(provider.layer("PLAIN").orElseThrow().declaresObservance());
        assertEquals(Optional.empty(), provider.layer("missing"));

        assertEquals(HolidayType.HALF_DAY, byName.get("Early close").type());
        assertEquals(LocalTime.of(13, 0), byName.get("Early close").closesAt());
        assertEquals(HolidayType.HALF_DAY, byName.get("Implied half day").type());
        assertEquals(LocalTime.of(14, 30), byName.get("Implied half day").closesAt());
        assertEquals(HolidayType.BANK, byName.get("Bank day").type());
        assertEquals(Year.of(2000), byName.get("Bank day").validity().from());
        assertEquals(Year.of(2030), byName.get("Bank day").validity().until());
        assertEquals(ObservanceRule.none(), byName.get("Bank day").observance());
        assertEquals(ObservanceRule.inherit(), byName.get("Day after").observance());

        var dayAfter = HolidayCalendar.builder()
                .layers(emirates)
                .build()
                .holidaysIn(Year.of(2026))
                .stream()
                .filter(occurrence -> occurrence.name().equals("Day after"))
                .findFirst()
                .orElseThrow();

        assertEquals(LocalDate.of(2026, 11, 27), dayAfter.original());
    }

    @Test
    void overrideAndRemoveRowsChangeTheParentLayer() {
        run("""
                INSERT INTO holiday_layer (id, parent_id) VALUES ('ES', NULL), ('ES-CT', 'ES');
                INSERT INTO holiday (layer_id, name, action, rule) VALUES
                    ('ES', 'Constitución', 'ADD', '{"fixed": "12-06"}'),
                    ('ES', 'Sant Joan', NULL, '{"fixed": "06-23"}'),
                    ('ES-CT', 'Constitución', 'REMOVE', NULL),
                    ('ES-CT', 'Sant Joan', 'OVERRIDE', '{"fixed": "06-24"}');
                """);

        var catalunya = CalendarRegistry.of(JdbcDataProvider.of(dataSource)).calendar("ES-CT");

        assertEquals(List.of(LocalDate.of(2026, 6, 24)), observedIn(catalunya, 2026));
    }

    @Test
    void readsTablesAndColumnsWithOtherNames() {
        run("""
                CREATE SCHEMA ref;
                CREATE TABLE ref.cal (code VARCHAR(20), base VARCHAR(20), rest_days VARCHAR(60), moves VARCHAR(40));
                CREATE TABLE ref.cal_day (cal VARCHAR(20), label VARCHAR(80), op VARCHAR(8), spec VARCHAR(200),
                    kind VARCHAR(16), first_year INTEGER, last_year INTEGER, moves VARCHAR(40), closes VARCHAR(5));
                INSERT INTO ref.cal VALUES ('BR', NULL, NULL, NULL);
                INSERT INTO ref.cal_day (cal, label, spec, first_year) VALUES ('BR', 'Consciência Negra', '{"fixed": "11-20"}', 2024);
                """);
        var schema = new JdbcSchema(
                new LayerTable("ref.cal", "code", "base", "rest_days", "moves"),
                new HolidayTable("ref.cal_day", "cal", "label", "op", "spec", "kind", "first_year", "last_year", "moves", "closes"));

        var brazil = CalendarRegistry.of(JdbcDataProvider.of(dataSource, schema)).calendar("BR");

        assertFalse(brazil.isHoliday(LocalDate.of(2023, 11, 20)));
        assertTrue(brazil.isHoliday(LocalDate.of(2024, 11, 20)));
    }

    @Test
    void withTablesKeepsTheDefaultColumns() {
        run("""
                ALTER TABLE holiday_layer RENAME TO cal_layer;
                ALTER TABLE holiday RENAME TO cal_holiday;
                INSERT INTO cal_layer (id) VALUES ('ES');
                INSERT INTO cal_holiday (layer_id, name, rule) VALUES ('ES', 'Navidad', '{"fixed": "12-25"}');
                """);

        var provider = JdbcDataProvider.of(dataSource, JdbcSchema.defaults().withTables("cal_layer", "cal_holiday"));

        assertEquals(Set.of("ES"), provider.layerIds());
        assertEquals(1, provider.layer("ES").orElseThrow().additions().size());
    }

    @Test
    void duplicateLayerIdFailsWithTheId() {
        run("""
                CREATE TABLE loose_layer (id VARCHAR(64), parent_id VARCHAR(64), weekend VARCHAR(64), observance VARCHAR(40));
                INSERT INTO loose_layer (id) VALUES ('ES'), ('PT'), ('ES');
                """);
        var schema = JdbcSchema.defaults().withTables("loose_layer", "holiday");

        var error = assertThrows(IllegalArgumentException.class, () -> JdbcDataProvider.of(dataSource, schema));

        assertEquals("layer id 'ES' is defined more than once in table loose_layer", error.getMessage());
    }

    @Test
    void badRowsFailNamingTheLayerAndTheHoliday() {
        assertRejected("('ES', 'Navidad', NULL, '{\"fixd\": \"12-25\"}', NULL, NULL, NULL, NULL, NULL)",
                "holiday 'Navidad' of layer 'ES' in table holiday", "exactly one of");
        assertRejected("('ES', 'Navidad', NULL, NULL, NULL, NULL, NULL, NULL, NULL)", "the rule is null");
        assertRejected("('ES', 'Navidad', 'UPSERT', '{\"fixed\": \"12-25\"}', NULL, NULL, NULL, NULL, NULL)",
                "unknown action \"UPSERT\"");
        assertRejected("('ES', 'Navidad', 'REMOVE', '{\"fixed\": \"12-25\"}', NULL, NULL, NULL, NULL, NULL)",
                "a REMOVE row takes only the layer and the name");
        assertRejected("('ES', 'Navidad', NULL, '{\"fixed\": \"12-25\"}', 'FESTIVE', NULL, NULL, NULL, NULL)", "FESTIVE");
        assertRejected("('ES', 'Navidad', NULL, '{\"fixed\": \"12-25\"}', NULL, 2030, 2020, NULL, NULL)", "'Navidad'");
        assertRejected("('ES', 'Navidad', NULL, '{\"fixed\": \"12-25\"}', NULL, NULL, NULL, 'MONDAY', NULL)",
                "unknown observance \"MONDAY\"");
        assertRejected("('ES', 'Navidad', NULL, '{\"fixed\": \"12-25\"}', NULL, NULL, NULL, NULL, 'noon')", "noon");
        assertRejected("('ES', 'Navidad', NULL, '{\"fixed\": \"12-25\"}', 'PUBLIC', NULL, NULL, NULL, '13:00')",
                "closes at needs type HALF_DAY");
        assertRejected("('PT', 'Natal', NULL, '{\"fixed\": \"12-25\"}', NULL, NULL, NULL, NULL, NULL)",
                "holiday 'Natal' of layer 'PT' in table holiday belongs to a layer that is not defined");
    }

    @Test
    void aBadRuleKeepsTheParserErrorAsCause() {
        run("""
                INSERT INTO holiday_layer (id) VALUES ('ES');
                INSERT INTO holiday (layer_id, name, rule) VALUES ('ES', 'Navidad', '{"fixed": "25 December"}');
                """);

        var error = assertThrows(IllegalArgumentException.class, () -> JdbcDataProvider.of(dataSource));

        assertInstanceOf(HolidayFormatException.class, error.getCause());
    }

    @Test
    void badLayersFailNamingTheLayer() {
        assertLayerRejected("('ES', 'ES', NULL, NULL)", "layer 'ES' in table holiday_layer", "cannot extend itself");
        assertLayerRejected("('ES', NULL, 'SATURDAY,SUN', NULL)", "layer 'ES' in table holiday_layer", "SUN");
        assertLayerRejected("('ES', NULL, NULL, 'INHERIT')", "a layer cannot declare observance INHERIT");
        assertLayerRejected("('ES', NULL, NULL, 'MONDAY')", "unknown observance \"MONDAY\"");
    }

    @Test
    void aHolidayAddedTwiceInOneLayerFails() {
        run("""
                CREATE TABLE loose_holiday (layer_id VARCHAR(64), name VARCHAR(200), action VARCHAR(8), rule VARCHAR(400),
                    type VARCHAR(16), valid_from INTEGER, valid_until INTEGER, observance VARCHAR(40), closes_at VARCHAR(5));
                INSERT INTO holiday_layer (id) VALUES ('ES');
                INSERT INTO loose_holiday (layer_id, name, rule) VALUES
                    ('ES', 'Navidad', '{"fixed": "12-25"}'), ('ES', 'Navidad', '{"fixed": "12-26"}'), ('ES', NULL, '{"fixed": "01-01"}');
                """);
        var twice = JdbcSchema.defaults().withTables("holiday_layer", "loose_holiday");

        var nullName = assertThrows(IllegalArgumentException.class, () -> JdbcDataProvider.of(dataSource, twice));

        assertTrue(nullName.getMessage().contains("the holiday name is null"), nullName.getMessage());

        run("DELETE FROM loose_holiday WHERE name IS NULL");
        var duplicate = assertThrows(IllegalArgumentException.class, () -> JdbcDataProvider.of(dataSource, twice));

        assertTrue(duplicate.getMessage().contains("'Navidad' more than once"), duplicate.getMessage());
    }

    @Test
    void aMissingTableFailsAsAnIllegalState() {
        var schema = JdbcSchema.defaults().withTables("no_such_table", "holiday");

        var error = assertThrows(IllegalStateException.class, () -> JdbcDataProvider.of(dataSource, schema));

        assertTrue(error.getMessage().contains("no_such_table"), error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void schemaNamesMustBePlainIdentifiers() {
        var defaults = JdbcSchema.defaults();

        assertThrows(IllegalArgumentException.class, () -> defaults.withTables("holiday_layer; DROP TABLE holiday", "holiday"));
        assertThrows(IllegalArgumentException.class, () -> defaults.withTables("holiday_layer", "holiday h"));
        assertThrows(IllegalArgumentException.class, () -> defaults.withTables("", "holiday"));
        assertThrows(IllegalArgumentException.class,
                () -> new LayerTable("holiday_layer", "id", "parent_id", "weekend", "observance --"));
        assertThrows(NullPointerException.class, () -> defaults.withTables(null, "holiday"));
        assertThrows(NullPointerException.class, () -> new JdbcSchema(null, defaults.holidays()));
        assertEquals("ref.holiday_layer", defaults.withTables("ref.holiday_layer", "ref.holiday").layers().table());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> JdbcDataProvider.of(null));
        assertThrows(NullPointerException.class, () -> JdbcDataProvider.of(dataSource, null));
    }

    private void assertRejected(String holidayRow, String... fragments) {
        run("""
                DELETE FROM holiday;
                DELETE FROM holiday_layer;
                INSERT INTO holiday_layer (id) VALUES ('ES');
                INSERT INTO holiday (layer_id, name, action, rule, type, valid_from, valid_until, observance, closes_at) VALUES
                """ + holidayRow);

        assertFails(fragments);
    }

    private void assertLayerRejected(String layerRow, String... fragments) {
        run("DELETE FROM holiday_layer; INSERT INTO holiday_layer (id, parent_id, weekend, observance) VALUES " + layerRow);

        assertFails(fragments);
    }

    private void assertFails(String... fragments) {
        var error = assertThrows(IllegalArgumentException.class, () -> JdbcDataProvider.of(dataSource));

        for (var fragment : fragments) {
            assertTrue(error.getMessage().contains(fragment), error.getMessage());
        }
    }

    private static List<LocalDate> observedIn(HolidayCalendar calendar, int year) {
        return calendar.holidaysIn(Year.of(year))
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();
    }

    private void run(String sql) {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
