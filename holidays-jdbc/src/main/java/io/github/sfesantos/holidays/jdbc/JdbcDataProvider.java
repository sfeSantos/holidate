package io.github.sfesantos.holidays.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayPeriod;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.LayerBuilder;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.format.HolidayFormat;
import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.jdbc.JdbcSchema.HolidayTable;
import io.github.sfesantos.holidays.jdbc.JdbcSchema.LayerTable;
import io.github.sfesantos.holidays.registry.HolidayDataProvider;

/**
 * Layers read from two database tables, one row per layer and one row per
 * holiday, or per period of a holiday that has several: rows of one layer
 * that share a name and an action become one holiday, and their years must
 * not overlap. {@link JdbcSchema} names the tables and documents the default
 * schema.
 * Both tables are read once, with one query each, when the provider is
 * created, and the connection is closed before {@code of} returns. A
 * duplicate layer id or a row that does not describe a valid holiday fails
 * there.
 *
 * <p>A holiday row holds its rule as text in the vocabulary of the JSON
 * format, for example {@code {"fixed": "01-01"}}. Its action is
 * {@code ADD}, {@code OVERRIDE} or {@code REMOVE}, and {@code ADD} when
 * null; a {@code REMOVE} row needs only the layer and the name.
 */
public final class JdbcDataProvider implements HolidayDataProvider {

    private final Map<String, Layer> layers;

    private JdbcDataProvider(Map<String, Layer> layers) {
        this.layers = Map.copyOf(layers);
    }

    public static JdbcDataProvider of(DataSource dataSource) {
        return of(dataSource, JdbcSchema.defaults());
    }

    public static JdbcDataProvider of(DataSource dataSource, JdbcSchema schema) {
        Objects.requireNonNull(dataSource, "dataSource must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        var builders = new LinkedHashMap<String, LayerBuilder>();

        try (var connection = dataSource.getConnection()) {
            readLayers(connection, schema.layers(), builders);
            readHolidays(connection, schema.holidays(), builders);
        } catch (SQLException e) {
            throw new IllegalStateException("could not read holiday layers from tables " + schema.layers().table()
                    + " and " + schema.holidays().table() + ": " + e.getMessage(), e);
        }

        var layers = new LinkedHashMap<String, Layer>();

        for (var entry : builders.entrySet()) {
            layers.put(entry.getKey(), build(entry.getKey(), entry.getValue(), schema));
        }

        return new JdbcDataProvider(layers);
    }

    @Override
    public Set<String> layerIds() {
        return layers.keySet();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return Optional.ofNullable(layers.get(id));
    }

    private static void readLayers(Connection connection, LayerTable table, Map<String, LayerBuilder> builders)
            throws SQLException {
        var sql = "SELECT " + String.join(", ", table.id(), table.parent(), table.weekend(), table.observance())
                + " FROM " + table.table() + " ORDER BY " + table.id();

        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                var id = rows.getString(1);
                var parent = rows.getString(2);
                var weekend = rows.getString(3);
                var observance = rows.getString(4);
                var builder = guarded("layer '" + id + "' in table " + table.table(), () -> layer(id, parent, weekend, observance));

                if (builders.putIfAbsent(id, builder) != null) {
                    throw new IllegalArgumentException("layer id '" + id + "' is defined more than once in table " + table.table());
                }
            }
        }
    }

    private static LayerBuilder layer(String id, String parent, String weekend, String observance) {
        if (id == null) {
            throw new IllegalArgumentException("the layer id is null");
        }

        var builder = Layer.named(id);

        if (parent != null) {
            builder.extending(parent);
        }

        if (weekend != null) {
            builder.weekend(weekend(weekend));
        }

        if (observance != null) {
            var rule = ObservanceRule.named(observance);

            if (rule == ObservanceRule.inherit()) {
                throw new IllegalArgumentException("a layer cannot declare observance INHERIT; leave the column null instead");
            }

            builder.observance(rule);
        }

        return builder;
    }

    private static Weekend weekend(String text) {
        var days = new ArrayList<DayOfWeek>();

        for (var name : text.split(",")) {
            if (!name.isBlank()) {
                days.add(DayOfWeek.valueOf(name.strip()));
            }
        }

        return Weekend.of(days.toArray(DayOfWeek[]::new));
    }

    private static void readHolidays(Connection connection, HolidayTable table, Map<String, LayerBuilder> builders)
            throws SQLException {
        var sql = "SELECT " + String.join(", ", table.layer(), table.name(), table.action(), table.rule(), table.type(),
                table.from(), table.until(), table.observance(), table.closesAt())
                + " FROM " + table.table() + " ORDER BY " + table.layer() + ", " + table.name() + ", " + table.from();

        var holidays = new LinkedHashMap<List<String>, GroupedHoliday>();

        try (var statement = connection.createStatement(); var rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                var row = HolidayRow.of(rows);
                var builder = builders.get(row.layer());
                var where = "holiday '" + row.name() + "' of layer '" + row.layer() + "' in table " + table.table();

                if (builder == null) {
                    throw new IllegalArgumentException(where + " belongs to a layer that is not defined");
                }

                guarded(where, () -> row.applyTo(builder, where, holidays));
            }
        }

        for (var grouped : holidays.values()) {
            guarded(grouped.where(), grouped::apply);
        }
    }

    private static Layer build(String id, LayerBuilder builder, JdbcSchema schema) {
        return guarded("layer '" + id + "' in tables " + schema.layers().table() + " and " + schema.holidays().table(),
                builder::build);
    }

    /** Runs a parser or builder call and adds the row it was reading to its failure. */
    private static <T> T guarded(String where, Call<T> call) {
        try {
            return call.run();
        } catch (IllegalArgumentException | IllegalStateException | DateTimeException | HolidayFormatException e) {
            throw new IllegalArgumentException(where + ": " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface Call<T> {

        T run();
    }

    private record HolidayRow(String layer, String name, String action, String rule, String type, Integer from,
            Integer until, String observance, String closesAt) {

        static HolidayRow of(ResultSet rows) throws SQLException {
            return new HolidayRow(rows.getString(1), rows.getString(2), rows.getString(3), rows.getString(4),
                    rows.getString(5), rows.getObject(6, Integer.class), rows.getObject(7, Integer.class),
                    rows.getString(8), rows.getString(9));
        }

        /** Rows that add or override under one name are the periods of one holiday, so they wait for each other. */
        LayerBuilder applyTo(LayerBuilder builder, String where, Map<List<String>, GroupedHoliday> holidays) {
            if (name == null) {
                throw new IllegalArgumentException("the holiday name is null");
            }

            var kind = action == null ? "ADD" : action;

            switch (kind) {
                case "ADD", "OVERRIDE" -> {
                    var period = period();
                    holidays.merge(List.of(layer, kind, name),
                            new GroupedHoliday(where, builder, kind.equals("OVERRIDE"), Holiday.of(name, period)),
                            (grouped, single) -> grouped.and(period));
                }
                case "REMOVE" -> builder.remove(removedName());
                default -> throw new IllegalArgumentException("unknown action \"" + action + "\"; expected ADD, OVERRIDE or REMOVE");
            }

            return builder;
        }

        private String removedName() {
            if (rule != null || type != null || from != null || until != null || observance != null || closesAt != null) {
                throw new IllegalArgumentException("a REMOVE row takes only the layer and the name");
            }

            return name;
        }

        private HolidayPeriod period() {
            if (rule == null) {
                throw new IllegalArgumentException("the rule is null");
            }

            var period = HolidayPeriod.of(HolidayFormat.json().parseRule(rule));
            var explicitType = type == null ? null : HolidayType.valueOf(type);

            if (explicitType != null) {
                period = period.ofType(explicitType);
            }

            if (from != null) {
                period = period.validFrom(Year.of(from));
            }

            if (until != null) {
                period = period.validUntil(Year.of(until));
            }

            if (observance != null) {
                period = period.withObservance(ObservanceRule.named(observance));
            }

            if (closesAt != null) {
                if (explicitType != null && explicitType != HolidayType.HALF_DAY) {
                    throw new IllegalArgumentException("closes at needs type HALF_DAY but the type is " + explicitType);
                }

                period = period.closingAt(LocalTime.parse(closesAt));
            }

            return period;
        }
    }

    private record GroupedHoliday(String where, LayerBuilder builder, boolean override, Holiday holiday) {

        GroupedHoliday and(HolidayPeriod period) {
            return new GroupedHoliday(where, builder, override, holiday.and(period));
        }

        LayerBuilder apply() {
            return override ? builder.override(holiday) : builder.add(holiday);
        }
    }
}
