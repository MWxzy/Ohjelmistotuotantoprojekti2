package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitDAOTest {

    private UnitDAO dao;

    @BeforeEach
    void setUp() {
        System.setProperty("h2.url",
                "jdbc:h2:mem:units" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        DatabaseManager.initializeDatabase();
        dao = new UnitDAO();
    }

    @AfterEach
    void clearDatabaseOverride() {
        System.clearProperty("h2.url");
    }

    @Test
    void findsTemperatureUnitsInEnglish() throws Exception {
        List<Unit> units = dao.findByCategory("TEMPERATURE", "en");

        assertEquals(3, units.size());
        assertEquals("celsius", units.get(0).key());
        assertEquals("Celsius", units.get(0).name());
        assertEquals("°C", units.get(0).symbol());
        assertEquals("fahrenheit", units.get(1).key());
        assertEquals("kelvin", units.get(2).key());
    }

    @Test
    void findsUnitsInFinnish() throws Exception {
        List<Unit> units = dao.findByCategory("DISTANCE", "fi");

        assertEquals("Metri", units.get(0).name());
        assertEquals("Kilometri", units.get(1).name());
        assertEquals("Maili", units.get(2).name());
    }

    @Test
    void findsUnitsInSwedish() throws Exception {
        List<Unit> units = dao.findByCategory("TIME", "sv");

        assertEquals("Sekund", units.get(0).name());
        assertEquals("Minut", units.get(1).name());
        assertEquals("Timme", units.get(2).name());
    }

    @Test
    void findsUnitsInJapanese() throws Exception {
        List<Unit> units = dao.findByCategory("TEMPERATURE", "ja");

        assertEquals("摂氏", units.get(0).name());
        assertEquals("華氏", units.get(1).name());
        assertEquals("ケルビン", units.get(2).name());
    }

    @Test
    void findsUnitsInArabic() throws Exception {
        List<Unit> units = dao.findByCategory("SPEED", "ar");

        assertEquals("متر في الثانية", units.get(0).name());
        assertEquals("كيلومتر في الساعة", units.get(1).name());
        assertEquals("ميل في الساعة", units.get(2).name());
    }

    @Test
    void fallsBackToEnglishForUnknownLanguage() throws Exception {
        Unit unit = dao.findByKey("celsius", "fr");

        assertEquals("Celsius", unit.name());
        assertEquals(
                "Degrees Celsius, the everyday temperature scale.",
                unit.description()
        );
    }

    @Test
    void fallsBackToEnglishWhenTranslationRowIsMissing() throws Exception {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM unit_translation WHERE unit_id = 1 AND lang = 'ja'"
             )) {
            statement.executeUpdate();
        }

        Unit unit = dao.findByKey("celsius", "ja");

        assertEquals("Celsius", unit.name());
    }

    @Test
    void findsUnitById() throws Exception {
        Unit unit = dao.findById(21, "en");

        assertEquals("Kilometre", unit.name());
        assertEquals("km", unit.symbol());
        assertEquals(1000.0, unit.factorToBase());
    }

    @Test
    void returnsNullForMissingUnit() throws Exception {
        assertNull(dao.findById(999, "en"));
        assertNull(dao.findByKey("missing", "en"));
    }

    @Test
    void returnsEmptyListForUnknownCategory() throws Exception {
        assertTrue(dao.findByCategory("UNKNOWN", "en").isEmpty());
    }
}
