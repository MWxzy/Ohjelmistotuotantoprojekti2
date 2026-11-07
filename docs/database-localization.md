# Database Localization Approach

## Chosen approach: translation table

The project uses a **translation table** for database localization. Language-independent unit data is stored once in `unit`, and each localized name and short description is stored in `unit_translation`.

```text
unit
  unit_id, unit_key, category, symbol, factor_to_base, offset_to_base, display_order

unit_translation
  unit_id, lang, name, description
  primary key (unit_id, lang)
  foreign key (unit_id) -> unit(unit_id)
```

The database stores five languages (`en`, `fi`, `sv`, `ja`, `ar`) for every unit. The current seed data contains 12 units and 60 translation rows.

## Why this approach was chosen

The translation table keeps the unit facts and the translations separate:

- Conversion factors and symbols are stored only once per unit.
- Adding a language requires inserting translation rows, not changing the table structure.
- The unit table does not become wider when a new language is added.
- The same pattern is used for unit names, descriptions, and history queries.
- It avoids duplicating conversion factors in every localized row, which would happen with a row-based approach.
- It avoids creating one table per language.

The static GUI labels are still localized with `.properties` resource bundles. Only localized *data* is stored in the database, which matches the assignment requirement.

## English fallback

`UnitDAO` joins the selected language and English, then uses `COALESCE` to select the first available value:

```sql
LEFT JOIN unit_translation t
       ON t.unit_id = u.unit_id AND t.lang = ?
LEFT JOIN unit_translation e
       ON e.unit_id = u.unit_id AND e.lang = 'en'
...
COALESCE(t.name, e.name) AS name,
COALESCE(t.description, e.description) AS description
```

If a translation row or one of its fields is missing, the English value is returned. The same fallback pattern is used by `ConversionDAO` and `MeasurementDAO` for the history lists.

## Measurement tables

The speed calculator writes measurements to three related tables:

- `distance_measurement` stores the distance value and its unit.
- `time_measurement` stores the time value and its unit.
- `speed_measurement` references both measurement rows and stores the calculated speed, speed unit, language, and timestamp.

The GUI reads the latest speed calculations back from these tables and displays them in the speed history list. Temperature conversions are stored in `conversion_history` with the input value, source unit, target unit, result, language, and timestamp.

## Verification

- `mvn clean verify` runs the test suite and the JaCoCo coverage checks.
- `UnitDAOTest` verifies English, Finnish, Swedish, Japanese, and Arabic names and the English fallback.
- `ConversionDAOTest` and `MeasurementDAOTest` verify that localized names and related measurement rows are stored and read correctly.
- `DatabaseManagerTest` verifies that `init.sql` is idempotent and creates 12 units and 60 translations.
