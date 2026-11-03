# Holidays — biblioteca Java de feriados, dias úteis e fins de semana

Biblioteca Java pura (zero dependências externas, só `java.base`) para definir calendários de feriados por camadas (nacional → regional → provincial → local), com regras de observância configuráveis e consultas rápidas sobre feriados e dias úteis.
 
---

## 1. Princípios não negociáveis

- [ ] **Zero dependências.** Apenas `java.base` (`java.time`, `java.util`, `java.util.function`, `java.util.concurrent`). Nem Jackson, nem Guava, nem Lombok, nem SLF4J.
- [ ] **Performance é prioridade total.** Toda consulta (`isHoliday`, `nextHoliday`, `isBusinessDay`) deve ser O(1) ou O(log n) sobre dados pré-computados por ano. Nunca recalcular regras numa consulta.
- [ ] **Imutabilidade.** Calendários, regras e ocorrências são imutáveis e thread-safe. Construção via builder; depois disso, só leitura.
- [ ] **Regras, não datas.** Um feriado é `Year -> Optional<LocalDate>`. As datas concretas são derivadas e cacheadas.
- [ ] **Core genérico, dados opcionais.** O motor não conhece Espanha nem Málaga. Catálogos de países são módulos separados ou classes utilitárias opcionais.
- [ ] **Integração nativa com `java.time`.** Expor `TemporalAdjuster` e `TemporalQuery` para que `date.with(calendar.nextBusinessDay())` funcione.
---

## 2. Regras de negócio

### 2.1 Tipos de regra de feriado (`sealed interface HolidayRule`)

- [x] `FixedDate` — dia e mês fixos (25/12, 1/1, 1/11).
- [x] `EasterRelative` — deslocamento em dias relativo ao Domingo de Páscoa (Sexta-feira Santa = −2, Quinta Santa = −3, Segunda de Páscoa = +1, Corpus Christi = +60).
- [x] `NthWeekdayOfMonth` — enésimo dia da semana do mês (4ª quinta de novembro, 1ª segunda de maio). Suportar `n` negativo para "último".
- [x] `WeekdayAfter` / `WeekdayBefore` — primeiro dia da semana X depois/antes de uma data base.
- [x] `Custom` — `Function<Year, Optional<LocalDate>>` para casos que não encaixam.
- [x] Toda regra aceita **intervalo de validade** (`validFrom`, `validUntil`, ambos opcionais, em `Year`). Fora do intervalo, a regra devolve `Optional.empty()`.
- [x] Toda regra tem **nome** e **tipo** (`PUBLIC`, `BANK`, `OBSERVANCE`, `HALF_DAY`) para o usuário filtrar o que conta como "dia não útil".
### 2.2 Cálculo da Páscoa

- [x] Algoritmo anónimo gregoriano (Meeus/Jones/Butcher). Implementado como função pura `GregorianEaster.of(int year)`.
- [x] Preparar ponto de extensão para Páscoa juliana/ortodoxa (`EasterAlgorithm` como interface com duas implementações). Não é prioridade, mas o design não deve impedir.
- [x] Testar contra tabela conhecida (2000–2100).
### 2.3 Observância (`ObservanceRule`)

Regra aplicada **depois** de calcular a data original. Não pertence ao feriado; pertence ao calendário (ou camada).

- [x] `none()` — sem deslocamento.
- [x] `sundayToMonday()` — regra típica de Espanha (comunidades autónomas decidem; Andalucía costuma aplicar).
- [x] `weekendToMonday()` — sábado ou domingo → segunda seguinte (Reino Unido, Austrália).
- [x] `saturdayToFridaySundayToMonday()` — modelo EUA federal.
- [x] `nearestWeekday()` — dia útil mais próximo.
- [x] `custom(UnaryOperator<LocalDate>)`.
- [x] Observância deve ser **composável** e **encadeável**: se o dia de destino também for feriado, avançar de novo (ex.: 25/12 e 26/12 ambos no fim de semana).
- [x] Permitir **override por feriado**: um feriado específico pode declarar `observance(ObservanceRule.none())` para ignorar a regra do calendário.
- [x] `HolidayOccurrence` guarda **data original** e **data observada** separadamente.
### 2.4 Camadas (`Layer`) e composição

- [x] Um `HolidayCalendar` é a composição ordenada de camadas: `Spain → Andalucía → Málaga (província) → Málaga (cidade)`.
- [x] Cada camada pode:
  - `add(rule)` — adicionar feriado.
  - `override(name, rule)` — substituir feriado da camada inferior pelo nome.
  - `remove(name)` — remover feriado herdado (ex.: Catalunha troca nacionais por regionais).
  - definir a sua própria `ObservanceRule` (sobrepõe a da camada abaixo).
- [x] Nomes de feriado são a chave de identidade entre camadas. Documentar isso.
- [x] Camadas são reutilizáveis: `Layer SPAIN` pode ser partilhada por 17 calendários regionais.
### 2.5 Fim de semana

- [ ] `Set<DayOfWeek>` configurável. Default `SATURDAY, SUNDAY`.
- [ ] Suportar `FRIDAY, SATURDAY` (Golfo) e `FRIDAY` só, ou `SUNDAY` só, ou vazio.
- [ ] Armazenar internamente como `EnumSet` / bitmask de 7 bits.
### 2.6 Consultas públicas (`HolidayCalendar`)

- [ ] `boolean isHoliday(LocalDate)`
- [ ] `boolean isWeekend(LocalDate)`
- [ ] `boolean isBusinessDay(LocalDate)` — não é feriado nem fim de semana.
- [ ] `Optional<HolidayOccurrence> holidayOn(LocalDate)`
- [ ] `Optional<HolidayOccurrence> nextHoliday(LocalDate)` / `previousHoliday(LocalDate)` — inclusivo ou exclusivo? Decidir e documentar (sugestão: exclusivo, com variante `nextHolidayInclusive`).
- [ ] `long daysUntilNextHoliday(LocalDate)`
- [ ] `List<HolidayOccurrence> holidaysIn(Year)` / `holidaysBetween(LocalDate, LocalDate)`
- [ ] `LocalDate nextBusinessDay(LocalDate)` / `previousBusinessDay(LocalDate)`
- [ ] `LocalDate addBusinessDays(LocalDate, int)` — suportar negativos.
- [ ] `long businessDaysBetween(LocalDate, LocalDate)`
- [ ] `TemporalAdjuster nextBusinessDayAdjuster()` etc.
- [ ] Versões com `Clock` injetável para "hoje" (`isHolidayToday(Clock)`), nunca `LocalDate.now()` hardcoded dentro da lib.
### 2.7 Exemplo de uso da API (referência de design)

Este é o alvo. Qualquer decisão de implementação que torne este código mais verboso deve ser justificada.

```java
// Camadas reutilizáveis — vivem em holidays-data-es, mas o usuário pode definir as suas
var spain = Layer.named("ES")
    .add(Holidays.NEW_YEAR, Holidays.EPIPHANY, Holidays.GOOD_FRIDAY,
         Holidays.LABOUR_DAY, Holidays.ASSUMPTION, Holidays.NATIONAL_DAY_ES,
         Holidays.ALL_SAINTS, Holidays.CONSTITUTION_ES, Holidays.IMMACULATE,
         Holidays.CHRISTMAS)
    .build();
 
var andalucia = Layer.named("ES-AN")
    .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28))
    .add(Holiday.easterRelative("Jueves Santo", -3))
    .observance(Observance.sundayToMonday())   // a comunidade decide mover
    .build();
 
var malaga = Layer.named("ES-AN-MA")
    .add(Holiday.fixed("San Ciriaco y Santa Paula", Month.JUNE, 18))
    .add(Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
    .build();
 
var calendar = HolidayCalendar.builder()
    .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    .layers(spain, andalucia, malaga)          // ordem = precedência
    .build();
 
// Consultas
var today = LocalDate.now(clock);
calendar.isHoliday(today);
calendar.isBusinessDay(today);
calendar.holidayOn(today);                     // Optional<HolidayOccurrence>
calendar.nextHoliday(today);                   // Optional<HolidayOccurrence>
calendar.daysUntilNextHoliday(today);          // long
calendar.holidaysIn(Year.of(2027));            // List<HolidayOccurrence>
calendar.addBusinessDays(today, 10);
calendar.businessDaysBetween(today, today.plusMonths(3));
 
// Integração com java.time
var deadline = today.with(calendar.nextBusinessDayAdjuster());
 
// Override e remoção numa camada (ex.: Catalunha)
var catalunya = Layer.named("ES-CT")
    .remove("Constitución")                    // nome herdado da camada ES
    .add(Holiday.fixed("Sant Joan", Month.JUNE, 24))
    .add(Holiday.fixed("Diada", Month.SEPTEMBER, 11))
    .build();
 
// Observância custom e override por feriado
var uk = Layer.named("GB")
    .observance(Observance.weekendToMonday())
    .add(Holidays.CHRISTMAS.withObservance(Observance.none()))
    .build();
 
// Regra custom
var rule = Holiday.custom("Election Day",
    year -> Optional.of(LocalDate.of(year.getValue(), Month.NOVEMBER, 1)
                                 .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
                                 .plusDays(1)))
    .validFrom(Year.of(1845));
```

### 2.8 Fora de escopo (v1)

- Horário comercial / meio-dia.
- Calendários não gregorianos (lunar, hebraico, islâmico).
- Download de dados de fontes externas.
- Persistência / serialização (pode entrar na v2 como formato próprio sem dependências).
---

## 3. Arquitetura

### 3.1 Módulos / pacotes

```
holidays-core/                      # motor de regras, sem dados de país
  io.github.<user>.holidays
    HolidayCalendar                 # fachada pública (interface)
    HolidayCalendarBuilder
    Layer, LayerBuilder
    HolidayRule (sealed) + records: FixedDate, EasterRelative, NthWeekdayOfMonth, WeekdayAfter, Custom
    ObservanceRule (interface funcional) + fábricas estáticas
    HolidayOccurrence (record)
    HolidayType (enum)
    Weekend (record sobre EnumSet<DayOfWeek>)
  io.github.<user>.holidays.easter
    EasterAlgorithm, GregorianEaster
  io.github.<user>.holidays.internal  # não exportado no module-info
    YearIndex                       # dados pré-computados de um ano
    CalendarCache
    CompiledLayer
 
holidays-data-es/   (opcional, módulo separado)
  Spain, Andalucia, Malaga, Catalunya...  # constantes de Layer
holidays-data-common/ (opcional)
  Holidays.CHRISTMAS, NEW_YEAR, GOOD_FRIDAY, ALL_SAINTS, EASTER_MONDAY...  # definições reutilizáveis, NÃO aplicadas por defeito
 
holidays-spring-boot-starter/   (opcional, módulo separado — ÚNICO com dependências externas)
  HolidayCalendarAutoConfiguration
  HolidayCalendarProperties           # @ConfigurationProperties("holidays")
  HolidayCalendarCustomizer           # interface funcional para o usuário ajustar o builder
  (ver secção 3.4)
```

- [x] `module-info.java` em cada módulo; exportar só a API pública; `internal` nunca exportado.
- [ ] `ServiceLoader<HolidayDataProvider>` para descoberta opcional de packs de dados sem acoplar o core.
- [x] Build: Maven multi-módulo, Java 25, `--release 25`. Sem plugins além dos essenciais (compiler, surefire, jar, javadoc).
### 3.2 Fluxo interno

```
Builder → valida → CompiledCalendar (imutável)
                       └─ CalendarCache: ConcurrentHashMap<Integer /*year*/, YearIndex>
                                           └─ computeIfAbsent(year, this::buildIndex)
 
YearIndex (imutável, por ano):
  - long[] / BitSet de 366 bits: isHoliday em O(1) por dayOfYear
  - HolidayOccurrence[] ordenado por data observada: nextHoliday via Arrays.binarySearch em O(log n)
  - Map<LocalDate, HolidayOccurrence> (ou array indexado por dayOfYear) para holidayOn em O(1)
```

- [ ] `buildIndex(year)`: para cada regra efetiva (após resolver camadas) → calcula data original → aplica observância → resolve colisões → ordena → congela.
- [x] Resolução de camadas (`add/override/remove`) acontece **uma vez** no `build()`, não por ano. Resultado: lista plana de regras efetivas + observância efetiva.
- [ ] `nextHoliday` que cruza a fronteira do ano consulta o índice do ano seguinte (lazy). Limitar busca a N anos (ex.: 10) para evitar loop infinito em calendário vazio; devolver `Optional.empty()`.
- [ ] `businessDaysBetween` sobre intervalos grandes: contar por ano inteiro usando contagem pré-computada no `YearIndex` (`businessDaysInYear`) e só iterar nas pontas.
### 3.3 Modelo de tipos (Java 25)

- [x] `sealed interface HolidayRule permits FixedDate, EasterRelative, NthWeekdayOfMonth, WeekdayAfter, Custom` + `switch` com pattern matching exaustivo em vez de `instanceof` em cascata.
- [ ] `record` para tudo o que é valor: `HolidayOccurrence(String name, LocalDate original, LocalDate observed, HolidayType type, String layer)`.
- [ ] Compact constructors nos records para validação (`Objects.requireNonNull`, ranges).
- [x] `ObservanceRule` como `@FunctionalInterface` para permitir lambdas.
- [ ] `Optional` só como tipo de retorno; nunca como campo ou parâmetro.
---

### 3.4 Integração com Spring (módulo opcional)

O core continua com zero dependências. Spring entra num starter separado que só agrega conveniência; quem não usa Spring nunca o vê.

- [ ] Módulo `holidays-spring-boot-starter`, Spring Boot 4, dependência `spring-boot-autoconfigure` + `spring-boot-starter` com `optional`/`provided` onde fizer sentido.
- [ ] `HolidayCalendarAutoConfiguration` registada em `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- [ ] `@ConditionalOnMissingBean(HolidayCalendar.class)` — se o usuário declarar o seu próprio bean, o starter recua.
- [ ] `@ConfigurationProperties("holidays")` com:
```yaml
  holidays:
    weekend: [SATURDAY, SUNDAY]
    layers: [ES, ES-AN, ES-AN-MA]      # ids resolvidos via ServiceLoader<HolidayDataProvider>
    observance: SUNDAY_TO_MONDAY
    warm-up-years: 3                   # pré-computar ano atual ± N
    clock: SYSTEM                      # ou zone id
```
- [ ] `HolidayCalendarCustomizer` (`@FunctionalInterface`, recebe o `HolidayCalendarBuilder`): o usuário regista um bean para adicionar camadas/regras em código sem perder a configuração por propriedades. Aplicar todos os customizers por `@Order`.
- [ ] Registar como bean o `HolidayCalendar` e também um `Clock` se não existir (`@ConditionalOnMissingBean(Clock.class)`).
- [ ] Suporte a **múltiplos calendários** nomeados: `holidays.calendars.es-malaga.layers: [...]`, `holidays.calendars.gb.layers: [...]` → beans qualificados por nome. Útil para apps multi-região.
- [ ] `WebFlux`-friendly: o calendário é imutável e síncrono (nanosegundos), não precisa de `Mono`. Documentar explicitamente que é seguro chamar em qualquer thread, incluindo event loop do Netty/Vert.x.
- [ ] Actuator (opcional, `@ConditionalOnClass`): `HealthIndicator` trivial + `InfoContributor` com camadas carregadas e anos em cache.
- [ ] Metadata para o IDE: `spring-boot-configuration-processor` como dependência de compilação, gera `additional-spring-configuration-metadata.json` para autocomplete no `application.yml`.
- [ ] Testes com `ApplicationContextRunner` (sem subir contexto web): auto-config liga/desliga, propriedades mapeadas, customizers aplicados em ordem, `@ConditionalOnMissingBean` respeitado.
- [ ] Sem `@Scheduled`, sem reload automático, sem cache de Spring — o cache é o do core.
- [ ] Nunca referenciar classes Spring fora deste módulo. Verificar no CI com um teste que o jar do core não tem `import org.springframework`.
  Exemplo de uso para o usuário final:

```java
@Bean
HolidayCalendarCustomizer localHolidays() {
    return builder -> builder.layer(Layer.named("my-office")
        .add(Holiday.fixed("Company Day", Month.MARCH, 15))
        .build());
}
 
@Service
class SettlementService {
    private final HolidayCalendar calendar;
    SettlementService(HolidayCalendar calendar) { this.calendar = calendar; }
 
    LocalDate settlementDate(LocalDate trade) {
        return calendar.addBusinessDays(trade, 2);   // T+2
    }
}
```

## 4. Boas práticas de código

### 4.1 Clean Code

- [ ] **Métodos de 10 a 15 linhas no máximo.** Se passar, extrair. Builders e `switch` de pattern matching podem ser a exceção documentada.
- [ ] Uma responsabilidade por classe (SRP). `YearIndex` só indexa; `GregorianEaster` só calcula Páscoa; `HolidayCalendarBuilder` só constrói.
- [ ] Nomes que dizem o quê, não o como: `nextBusinessDay`, não `findNextNonHolidayNonWeekendDate`.
- [ ] Sem flags booleanas em parâmetros (`nextHoliday(date, true)`). Criar dois métodos ou um enum.
- [ ] Máximo 3 parâmetros por método; acima disso, record de parâmetros.
- [ ] Sem comentários que explicam o código; comentários só para o *porquê* (ex.: referência ao BOE, ao algoritmo da Páscoa).
- [ ] Sem `null` na API pública. Entrada: `Objects.requireNonNull` com mensagem. Saída: `Optional` ou coleção vazia.
- [ ] Fail-fast no `build()`: nomes duplicados na mesma camada, `remove` de nome inexistente, `validFrom > validUntil` → `IllegalArgumentException` / `IllegalStateException` com mensagem clara.
- [ ] Exceções próprias só se agregarem valor (`HolidayCalendarException` opcional); nunca checked.
### 4.2 Uso de `var`

- [ ] Usar `var` em variáveis locais **quando o tipo é óbvio pelo lado direito** (`var calendar = HolidayCalendar.builder()...build();`, `var easter = GregorianEaster.of(year);`).
- [ ] Não usar `var` quando o tipo não é evidente (`var result = service.compute(x);`) ou quando o tipo explícito documenta a intenção (`List<HolidayOccurrence> sorted = ...`).
- [ ] Nunca `var` com literais numéricos ambíguos (`var n = 1;` vs `long`).
- [ ] Nunca `var` em campos, parâmetros ou retornos (a linguagem não permite, mas fica a regra).
### 4.3 SOLID aplicado

- [ ] **S** — ver 4.1.
- [ ] **O** — novas regras de feriado entram como novo `permits` do sealed interface + um case no switch; novas observâncias entram como lambda sem tocar no core.
- [ ] **L** — `HolidayCalendar` é interface; qualquer implementação (a compilada, uma composta, uma decorada com logging) deve respeitar o contrato documentado no Javadoc.
- [ ] **I** — separar interfaces de leitura: `HolidayQueries`, `BusinessDayQueries`, e `HolidayCalendar extends` ambas. Quem só precisa de dias úteis não vê o resto.
- [ ] **D** — `Clock` injetável; `EasterAlgorithm` injetável no builder; nada instancia dependências concretas dentro de métodos.
### 4.4 Imutabilidade e concorrência

- [ ] Todos os campos `final`. Coleções expostas via `List.copyOf` / `Collections.unmodifiableList`; nunca devolver o array interno.
- [ ] `ConcurrentHashMap.computeIfAbsent` no cache de anos; `YearIndex` imutável após construção → publicação segura garantida.
- [ ] Sem `synchronized`, sem locks explícitos.
- [ ] Builders **não** são thread-safe (documentar). Calendários construídos **são**.
### 4.5 API pública

- [ ] Fluente, estática e descobrível: `HolidayCalendar.builder()`, `Holiday.fixed(...)`, `Holiday.easterRelative(...)`, `Observance.sundayToMonday()`.
- [ ] Nomes em inglês na API; nomes de feriados em texto livre (o usuário escolhe o idioma).
- [ ] Javadoc completo em tudo o que é `public`, com exemplo de uso na classe principal.
- [ ] Tudo o que não é API é `package-private` ou está em `internal` não exportado.
- [ ] Versionamento semântico; nada em `public` muda sem major bump.
- [ ] `toString`, `equals`, `hashCode` consistentes (records dão de graça).
---

## 5. Performance (prioridade total)

### 5.1 Objetivos mensuráveis

- [ ] `isHoliday(date)` com ano já cacheado: < 20 ns, zero alocação.
- [ ] `nextHoliday(date)`: < 100 ns, no máximo uma alocação (o `Optional`).
- [ ] `buildIndex(year)` para calendário de ~20 feriados: < 50 µs.
- [ ] `businessDaysBetween` sobre 10 anos: < 5 µs usando contagens anuais pré-computadas.
- [ ] Cache de 100 anos de um calendário com 20 feriados: < 500 KB de heap.
### 5.2 Técnicas

- [ ] **Índice por `dayOfYear`**: arrays de tamanho 367 (índice 1..366) em vez de `Map<LocalDate, ...>` no hot path. `BitSet` ou `long[6]` para `isHoliday`; `HolidayOccurrence[367]` para `holidayOn`.
- [ ] **Bitmask para fim de semana**: `int weekendMask` com 7 bits; `isWeekend = (mask & (1 << dow.ordinal())) != 0`.
- [ ] **Binary search** sobre `int[] observedDayOfYear` ordenado para `nextHoliday`/`previousHoliday`.
- [ ] **Contagens anuais pré-computadas** no `YearIndex`: `businessDaysInYear`, `holidaysInYear`, e array de prefix sums `businessDaysUntil[dayOfYear]` para `businessDaysBetween` em O(1) dentro do mesmo ano.
- [ ] **`addBusinessDays(date, n)`** com `n` grande: saltar anos inteiros via `businessDaysInYear` e só iterar na ponta.
- [ ] **Lazy por ano** — nunca pré-computar intervalos que o usuário não pediu. Oferecer `warmUp(Year from, Year to)` opcional.
- [ ] **Sem Streams no hot path.** Streams só no `build()` e em métodos que já devolvem listas. Loops `for` indexados nas consultas.
- [ ] **Sem boxing**: `int year` como chave do cache (`ConcurrentHashMap<Integer, ...>` boxa; avaliar array esparso ou `Int2ObjectMap` caseiro com open addressing se o benchmark justificar).
- [ ] **Evitar `LocalDate` intermédios** nas consultas quando `dayOfYear`/`year` chegam.
- [ ] **Resolução de camadas no `build()`**, nunca na consulta (ver 3.2).
- [x] **Observância encadeada** limitada a 7 iterações (uma semana) para garantir terminação.
- [ ] **Sem reflexão, sem proxies, sem `String.format` em caminhos quentes.**
- [ ] Medir tudo com **JMH** (dependência só de teste/benchmark, fora do artefacto publicado) antes e depois de cada otimização. Não otimizar sem número.
- [ ] Testar com `-XX:+UseSerialGC` e heap pequeno para confirmar que o uso de memória é previsível.
---

## 6. Testes

- [ ] JUnit 5 (dependência só de teste). Sem Mockito — tudo é valor imutável, não precisa de mocks.
- [x] Páscoa: tabela 2000–2100 contra fonte oficial.
- [x] Cada tipo de regra: casos de ano bissexto, 29/02, limites de `validFrom/validUntil`.
- [x] Observância: colisões (25/12 + 26/12 no fim de semana), encadeamento, override por feriado, terminação em 7 passos.
- [x] Camadas: `add/override/remove`, ordem de precedência, remoção de nome inexistente falha no build.
- [ ] Consultas: fronteira de ano (`nextHoliday` em 30/12), calendário vazio devolve `Optional.empty()`, `addBusinessDays` negativo, `businessDaysBetween` com `a > b`.
- [ ] Fim de semana sexta-sábado.
- [ ] Concorrência: 100 threads a consultar o mesmo calendário em anos diferentes sem erro e com um único `buildIndex` por ano (contador atómico no teste).
- [ ] Dados de Espanha/Andalucía/Málaga validados contra o calendário laboral do BOE e BOJA de pelo menos 3 anos (2025, 2026, 2027). Testes nomeados pela fonte.
- [ ] Cobertura mínima 90% no core (JaCoCo, só em CI).
- [ ] Benchmarks JMH num módulo `holidays-benchmarks` fora da publicação.
---

## 7. Roadmap

### v0.1 — core
- [x] `module-info`, estrutura multi-módulo, CI (build + testes).
- [x] `GregorianEaster`.
- [x] `HolidayRule` sealed + 5 implementações + `validFrom/validUntil`.
- [x] `ObservanceRule` + fábricas + encadeamento.
- [x] `Layer` + `add/override/remove`.
- [ ] `HolidayCalendarBuilder` com validação fail-fast.
- [ ] `YearIndex` + `CalendarCache`.
- [ ] Consultas básicas: `isHoliday`, `isWeekend`, `isBusinessDay`, `holidayOn`, `holidaysIn`.
### v0.2 — navegação
- [ ] `nextHoliday`, `previousHoliday`, `daysUntilNextHoliday`.
- [ ] `nextBusinessDay`, `previousBusinessDay`, `addBusinessDays`, `businessDaysBetween` com prefix sums.
- [ ] `TemporalAdjuster` / `TemporalQuery`.
- [ ] `Clock` injetável.
### v0.3 — dados e extensão
- [ ] `holidays-data-common` (definições reutilizáveis, sem aplicação automática).
- [ ] `holidays-data-es`: Espanha + Andalucía + Málaga validados contra BOE/BOJA.
- [ ] `ServiceLoader<HolidayDataProvider>`.
- [ ] Javadoc completo + README com exemplo de Málaga.
### v0.4 — Spring
- [ ] `holidays-spring-boot-starter` com auto-config, properties, customizer e múltiplos calendários.
- [ ] Testes com `ApplicationContextRunner`.
- [ ] Teste de CI que garante zero `org.springframework` no jar do core.
### v1.0 — estabilização
- [ ] Benchmarks JMH publicados no README.
- [ ] Revisão de API pública (nada muda depois).
- [ ] Publicação em Maven Central.
### Depois
- [ ] Páscoa juliana/ortodoxa.
- [ ] Serialização própria (texto simples, sem dependências) para carregar calendários de ficheiro.
- [ ] Meio-dia / `HALF_DAY` com semântica real.
- [ ] Mais packs de países.
---

## 8. Decisões em aberto

- [ ] `nextHoliday(date)` é inclusivo ou exclusivo? (sugestão: exclusivo + variante inclusiva)
- [ ] Feriados de tipo `OBSERVANCE` (não folga) contam ou não em `isBusinessDay`? (sugestão: não; só `PUBLIC` e `BANK`, configurável no builder)
- [x] Chave de identidade entre camadas: `String name` ou um `HolidayId` tipado? (sugestão: começar com `String`, trocar se doer)
- [x] Nome do groupId / namespace do projeto: `io.github.sfesantos`, pacote `io.github.sfesantos.holidays`.
- [x] Maven vs Gradle: Maven.