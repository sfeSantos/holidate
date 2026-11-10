package io.github.sfesantos.holidays.jdbc;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The tables and columns a {@link JdbcDataProvider} reads. The names go into
 * the SQL text as they are, so each one must be a plain identifier,
 * optionally prefixed by a schema: letters, digits, {@code _} and {@code $},
 * parts separated by a dot.
 *
 * <p>{@link #defaults()} matches this schema:
 *
 * <pre>{@code
 * CREATE TABLE holiday_layer (
 *     id          VARCHAR(64) PRIMARY KEY,
 *     parent_id   VARCHAR(64),
 *     weekend     VARCHAR(64),     -- 'SATURDAY,SUNDAY'; NULL leaves it to the layers below
 *     observance  VARCHAR(40)      -- 'SUNDAY_TO_MONDAY'
 * );
 *
 * CREATE TABLE holiday (
 *     layer_id    VARCHAR(64)  NOT NULL,
 *     name        VARCHAR(200) NOT NULL,
 *     action      VARCHAR(8),      -- ADD (default when NULL), OVERRIDE or REMOVE
 *     rule        VARCHAR(400),    -- '{"fixed": "01-01"}', as "rule" in a JSON file
 *     type        VARCHAR(16),
 *     valid_from  INTEGER,
 *     valid_until INTEGER,
 *     observance  VARCHAR(40),
 *     closes_at   VARCHAR(5),      -- '13:00'
 *     PRIMARY KEY (layer_id, name)
 * );
 * }</pre>
 */
public record JdbcSchema(LayerTable layers, HolidayTable holidays) {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*(\\.[A-Za-z_][A-Za-z0-9_$]*)*");

    public JdbcSchema {
        Objects.requireNonNull(layers, "layers must not be null");
        Objects.requireNonNull(holidays, "holidays must not be null");
    }

    public static JdbcSchema defaults() {
        return new JdbcSchema(
                new LayerTable("holiday_layer", "id", "parent_id", "weekend", "observance"),
                new HolidayTable("holiday", "layer_id", "name", "action", "rule", "type", "valid_from", "valid_until",
                        "observance", "closes_at"));
    }

    /** The same columns in tables with other names. */
    public JdbcSchema withTables(String layerTable, String holidayTable) {
        return new JdbcSchema(
                new LayerTable(layerTable, layers.id(), layers.parent(), layers.weekend(), layers.observance()),
                new HolidayTable(holidayTable, holidays.layer(), holidays.name(), holidays.action(), holidays.rule(),
                        holidays.type(), holidays.from(), holidays.until(), holidays.observance(), holidays.closesAt()));
    }

    /** The table with one row per layer; every argument but {@code table} is a column name. */
    public record LayerTable(String table, String id, String parent, String weekend, String observance) {

        public LayerTable {
            identifier(table, "table");
            identifier(id, "id");
            identifier(parent, "parent");
            identifier(weekend, "weekend");
            identifier(observance, "observance");
        }
    }

    /** The table with one row per holiday of a layer; every argument but {@code table} is a column name. */
    public record HolidayTable(String table, String layer, String name, String action, String rule, String type,
            String from, String until, String observance, String closesAt) {

        public HolidayTable {
            identifier(table, "table");
            identifier(layer, "layer");
            identifier(name, "name");
            identifier(action, "action");
            identifier(rule, "rule");
            identifier(type, "type");
            identifier(from, "from");
            identifier(until, "until");
            identifier(observance, "observance");
            identifier(closesAt, "closesAt");
        }
    }

    private static void identifier(String value, String what) {
        Objects.requireNonNull(value, what + " must not be null");

        if (!IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(what + " is not a plain SQL identifier: " + value);
        }
    }
}
