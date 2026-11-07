import database.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnitConverterTest {

    private static final Unit CELSIUS =
            unit(1, "TEMPERATURE", "°C", 1.0, 0.0);

    private static final Unit FAHRENHEIT =
            unit(2, "TEMPERATURE", "°F", 5.0 / 9.0, -32.0 * 5.0 / 9.0);

    private static final Unit KELVIN =
            unit(3, "TEMPERATURE", "K", 1.0, -273.15);

    private static final Unit METRE =
            unit(20, "DISTANCE", "m", 1.0, 0.0);

    private static final Unit KILOMETRE =
            unit(21, "DISTANCE", "km", 1000.0, 0.0);

    private static final Unit MILE =
            unit(22, "DISTANCE", "mi", 1609.344, 0.0);

    private static final Unit SECOND =
            unit(30, "TIME", "s", 1.0, 0.0);

    private static final Unit MINUTE =
            unit(31, "TIME", "min", 60.0, 0.0);

    private static final Unit HOUR =
            unit(32, "TIME", "h", 3600.0, 0.0);

    private static final Unit METRES_PER_SECOND =
            unit(10, "SPEED", "m/s", 1.0, 0.0);

    private static final Unit KILOMETRES_PER_HOUR =
            unit(11, "SPEED", "km/h", 1000.0 / 3600.0, 0.0);

    private static final Unit MILES_PER_HOUR =
            unit(12, "SPEED", "mph", 1609.344 / 3600.0, 0.0);

    private final UnitConverter converter = new UnitConverter();

    @Test
    void convertsCelsiusToFahrenheit() {
        assertEquals(212.0,
                converter.convertTemperature(100.0, CELSIUS, FAHRENHEIT),
                0.000001);
    }

    @Test
    void convertsFahrenheitToCelsius() {
        assertEquals(0.0,
                converter.convertTemperature(32.0, FAHRENHEIT, CELSIUS),
                0.000001);
    }

    @Test
    void convertsCelsiusToKelvin() {
        assertEquals(273.15,
                converter.convertTemperature(0.0, CELSIUS, KELVIN),
                0.000001);
    }

    @Test
    void convertsKelvinToCelsius() {
        assertEquals(0.0,
                converter.convertTemperature(273.15, KELVIN, CELSIUS),
                0.000001);
    }

    @Test
    void convertsMinusFortyBetweenCelsiusAndFahrenheit() {
        assertEquals(-40.0,
                converter.convertTemperature(-40.0, CELSIUS, FAHRENHEIT),
                0.000001);
    }

    @Test
    void convertsLinearDistanceUnits() {
        assertEquals(1000.0,
                converter.convertLinear(1.0, KILOMETRE, METRE),
                0.000001);
        assertEquals(1.609344,
                converter.convertLinear(1.0, MILE, KILOMETRE),
                0.000001);
    }

    @Test
    void convertsLinearTimeUnits() {
        assertEquals(1.0,
                converter.convertLinear(60.0, MINUTE, HOUR),
                0.000001);
        assertEquals(120.0,
                converter.convertLinear(2.0, MINUTE, SECOND),
                0.000001);
    }

    @Test
    void calculatesKilometresPerHour() {
        assertEquals(20.0,
                converter.calculateSpeed(
                        10.0,
                        KILOMETRE,
                        30.0,
                        MINUTE,
                        KILOMETRES_PER_HOUR
                ),
                0.000001);
    }

    @Test
    void calculatesMetresPerSecond() {
        assertEquals(100.0 / 3.6,
                converter.calculateSpeed(
                        100.0,
                        KILOMETRE,
                        1.0,
                        HOUR,
                        METRES_PER_SECOND
                ),
                0.000001);
    }

    @Test
    void calculatesMilesPerHour() {
        assertEquals(1.0,
                converter.calculateSpeed(
                        1.0,
                        MILE,
                        1.0,
                        HOUR,
                        MILES_PER_HOUR
                ),
                0.000001);
    }

    @Test
    void calculatesZeroSpeedForZeroDistance() {
        assertEquals(0.0,
                converter.calculateSpeed(
                        0.0,
                        METRE,
                        10.0,
                        SECOND,
                        METRES_PER_SECOND
                ),
                0.0);
    }

    @Test
    void rejectsZeroTime() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.calculateSpeed(
                        10.0,
                        METRE,
                        0.0,
                        SECOND,
                        METRES_PER_SECOND
                ));
    }

    @Test
    void rejectsNegativeDistance() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.calculateSpeed(
                        -1.0,
                        METRE,
                        1.0,
                        SECOND,
                        METRES_PER_SECOND
                ));
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertTemperature(
                        Double.NaN,
                        CELSIUS,
                        FAHRENHEIT
                ));
        assertThrows(IllegalArgumentException.class,
                () -> converter.calculateSpeed(
                        1.0,
                        METRE,
                        Double.POSITIVE_INFINITY,
                        SECOND,
                        METRES_PER_SECOND
                ));
    }

    @Test
    void rejectsWrongCategories() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertTemperature(1.0, METRE, CELSIUS));
        assertThrows(IllegalArgumentException.class,
                () -> converter.calculateSpeed(
                        1.0,
                        METRE,
                        1.0,
                        SECOND,
                        HOUR
                ));
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertLinear(1.0, METRE, SECOND));
    }

    @Test
    void rejectsNullUnit() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convertTemperature(1.0, null, CELSIUS));
    }

    @Test
    void rejectsInvalidConversionFactors() {
        Unit brokenDistance = unit(99, "DISTANCE", "x", 0.0, 0.0);

        assertThrows(IllegalArgumentException.class,
                () -> converter.convertLinear(1.0, brokenDistance, METRE));
        assertThrows(IllegalArgumentException.class,
                () -> converter.calculateSpeed(
                        1.0,
                        METRE,
                        1.0,
                        SECOND,
                        unit(100, "SPEED", "x", 0.0, 0.0)
                ));
    }

    private static Unit unit(
            int id,
            String category,
            String symbol,
            double factor,
            double offset
    ) {
        return new Unit(id, "unit_" + id, category, symbol, factor, offset, symbol, symbol);
    }
}
