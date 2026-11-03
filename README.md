# Holidays

A dependency-free Java library for holiday calendars and business day queries.

You describe holidays as rules (a fixed day, an offset from Easter, the fourth
Thursday of November) and stack them in layers (country, region, province,
city). The library resolves the rules once per year, applies the observance
rule of your region (Sunday to Monday, weekend to Monday, nearest weekday) and
answers questions like "is this a business day?" from precomputed indexes.

- Zero dependencies: only `java.base`.
- Immutable and thread-safe calendars, built once with a builder.
- Queries are O(1) or O(log n) over per-year indexes, computed lazily and cached.
- The core knows nothing about any country. Country data is yours to define or
  will ship as optional modules.

Requires Java 25.

## Status

Early. Version 0.1 covers rules, layers, observance and the basic queries shown
below. Navigation (`nextHoliday`, `addBusinessDays`, `businessDaysBetween`,
`TemporalAdjuster` integration) and country data packs are next. The public
API may still change before 1.0. See [TODO.md](TODO.md) for the roadmap.

## Install

Not yet on Maven Central. Build it locally:

```sh
mvn install
```

```xml
<dependency>
    <groupId>io.github.sfesantos</groupId>
    <artifactId>holidays-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Example: Málaga, Spain

Spain has national holidays, each autonomous community adds its own and
decides whether a holiday that falls on a Sunday moves to Monday (Andalucía
does), and each city adds two local holidays. That maps directly to three layers.

```java
import io.github.sfesantos.holidays.*;

import java.time.*;

var spain = Layer.named("ES")
        .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                Holiday.fixed("Epifanía del Señor", Month.JANUARY, 6),
                Holiday.easterRelative("Viernes Santo", -2),
                Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                Holiday.fixed("Asunción de la Virgen", Month.AUGUST, 15),
                Holiday.fixed("Fiesta Nacional de España", Month.OCTOBER, 12),
                Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                Holiday.fixed("Día de la Constitución", Month.DECEMBER, 6),
                Holiday.fixed("Inmaculada Concepción", Month.DECEMBER, 8),
                Holiday.fixed("Navidad", Month.DECEMBER, 25))
        .build();

var andalucia = Layer.named("ES-AN")
        .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28),
                Holiday.easterRelative("Jueves Santo", -3))
        .observance(ObservanceRule.sundayToMonday())
        .build();

var malaga = Layer.named("ES-AN-MA")
        .add(Holiday.fixed("Incorporación de Málaga a la Corona de Castilla", Month.AUGUST, 19),
                Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
        .build();

var calendar = HolidayCalendar.builder()
        .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        .layers(spain, andalucia, malaga)   // order is precedence, last wins
        .build();
```

Then ask:

```java
calendar.isHoliday(LocalDate.of(2026, 4, 2));        // true, Jueves Santo
calendar.isBusinessDay(LocalDate.of(2026, 4, 4));    // false, Saturday

// 1 November 2026 is a Sunday; Andalucía observes it on Monday
calendar.isHoliday(LocalDate.of(2026, 11, 1));       // false
calendar.holidayOn(LocalDate.of(2026, 11, 2));
// Optional[HolidayOccurrence[name=Todos los Santos, original=2026-11-01,
//                            observed=2026-11-02, type=PUBLIC, layer=ES]]

calendar.holidaysIn(Year.of(2026));                  // 14 occurrences, by date
```

The 2026 output matches the calendars published in the BOE and the BOJA.
The test [MalagaExampleTest](holidays-core/src/test/java/io/github/sfesantos/holidays/examples/MalagaExampleTest.java)
keeps it that way.

## Example: Rio de Janeiro, Brazil

Brazil never moves a holiday, so no observance rule is needed. What it has
instead is holiday types: Carnival and Corpus Christi are a national "ponto
facultativo" (optional day off), the state of Rio de Janeiro makes Carnival
Tuesday a real holiday, and the city gives Ash Wednesday off until 2 pm.
Black Awareness Day was a state holiday for years before it became national
in 2024.

```java
var brazil = Layer.named("BR")
        .add(Holiday.fixed("Confraternização Universal", Month.JANUARY, 1),
                Holiday.easterRelative("Segunda-feira de Carnaval", -48).ofType(HolidayType.OBSERVANCE),
                Holiday.easterRelative("Terça-feira de Carnaval", -47).ofType(HolidayType.OBSERVANCE),
                Holiday.easterRelative("Sexta-feira Santa", -2),
                Holiday.fixed("Tiradentes", Month.APRIL, 21),
                Holiday.fixed("Dia do Trabalho", Month.MAY, 1),
                Holiday.easterRelative("Corpus Christi", 60).ofType(HolidayType.OBSERVANCE),
                Holiday.fixed("Independência do Brasil", Month.SEPTEMBER, 7),
                Holiday.fixed("Nossa Senhora Aparecida", Month.OCTOBER, 12),
                Holiday.fixed("Finados", Month.NOVEMBER, 2),
                Holiday.fixed("Proclamação da República", Month.NOVEMBER, 15),
                Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2024)),
                Holiday.fixed("Natal", Month.DECEMBER, 25))
        .build();

var rioState = Layer.named("BR-RJ")
        .add(Holiday.fixed("São Jorge", Month.APRIL, 23))
        .override(Holiday.easterRelative("Terça-feira de Carnaval", -47))                  // now PUBLIC
        .override(Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2002)))
        .build();

var rioCity = Layer.named("BR-RJ-RIO")
        .add(Holiday.fixed("São Sebastião", Month.JANUARY, 20),
                Holiday.easterRelative("Quarta-feira de Cinzas", -46).ofType(HolidayType.HALF_DAY))
        .build();

var rio = HolidayCalendar.builder()
        .layers(brazil, rioState, rioCity)
        .build();
```

```java
rio.isHoliday(LocalDate.of(2026, 2, 17));       // true, Carnival Tuesday
rio.isBusinessDay(LocalDate.of(2026, 2, 17));   // false, PUBLIC in the state layer
rio.isBusinessDay(LocalDate.of(2026, 2, 16));   // true, Monday is only OBSERVANCE
rio.isBusinessDay(LocalDate.of(2026, 2, 18));   // true, HALF_DAY still counts as working

rio.isHoliday(LocalDate.of(2023, 11, 20));      // true, state holiday before it went national
rio.holidayOn(LocalDate.of(2028, 2, 29));       // Carnival Tuesday lands on a leap day
```

See [RioDeJaneiroExampleTest](holidays-core/src/test/java/io/github/sfesantos/holidays/examples/RioDeJaneiroExampleTest.java).

## Concepts

**Rules.** A `HolidayRule` turns a year into at most one date:
`fixed`, `easterRelative`, `nthWeekdayOfMonth` (negative `n` counts from the
end), `weekdayAfter`, `weekdayBefore` and `custom(Function<Year, Optional<LocalDate>>)`.
A `Holiday` is a rule plus a name, a `HolidayType` and a validity range
(`validFrom`, `validUntil`).

**Types.** `PUBLIC` and `BANK` make a day non-working. `OBSERVANCE` and
`HALF_DAY` show up in `isHoliday` and `holidayOn` but leave `isBusinessDay`
true. Change the set with `nonWorkingTypes(...)` on the builder.

**Layers.** A `Layer` adds holidays, overrides inherited ones by name or
removes them. Adding a name that a lower layer already defines is an error:
say `override` or `remove` explicitly. Layers are immutable and can be shared
between calendars.

**Observance.** An `ObservanceRule` moves a holiday off a day on which it is
not observed. The rule of the highest layer that declares one applies to the
whole calendar; a single holiday can opt out with
`withObservance(ObservanceRule.none())`. Rules chain, so when Christmas and
Boxing Day both fall on a weekend under `weekendToMonday()` they land on
Monday and Tuesday. A holiday may even move into the neighbouring year (1
January on a Saturday under the US rule is observed on 31 December).

**Occurrences.** Every query returns `HolidayOccurrence` records carrying
the original date, the observed date, the type and the layer that defined
the holiday.

**Weekend.** Any set of days: `weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)`
for the Gulf, a single day, or none.

**Easter.** Gregorian by default. Plug a different `EasterAlgorithm` into the
builder for Julian or Orthodox dates.

## Thread safety

Builders are not thread-safe. A built `HolidayCalendar` is immutable and may
be shared and queried from any thread, including event loops. Each year's
index is computed on first use and cached; `warmUp(from, until)` precomputes
a range ahead of time.

## License

Apache License 2.0. See [LICENSE](LICENSE).
