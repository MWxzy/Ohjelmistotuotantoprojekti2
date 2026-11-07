package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeasurementDAOTest {

    private MeasurementDAO dao;

    @BeforeEach
    void setUp() {
        System.setProperty("h2.url",
                "jdbc:h2:mem:measurements" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        DatabaseManager.initializeDatabase();
        dao = new MeasurementDAO();
    }

    @AfterEach
    void clearDatabaseOverride() {
        System.clearProperty("h2.url");
    }

    @Test
    void savesAndFindsSpeedCalculations() throws Exception {
        long id = dao.saveSpeed(10.0, 21, 30.0, 31, 20.0, 11, "en");

        assertTrue(id > 0);

        List<SpeedRecord> records = dao.findRecentSpeed(5, "en");

        assertEquals(1, records.size());

        SpeedRecord record = records.get(0);
        assertEquals(10.0, record.distanceValue());
        assertEquals("Kilometre", record.distanceName());
        assertEquals("km", record.distanceSymbol());
        assertEquals(30.0, record.timeValue());
        assertEquals("Minute", record.timeName());
        assertEquals("min", record.timeSymbol());
        assertEquals(20.0, record.speedValue());
        assertEquals("Kilometres per hour", record.speedName());
        assertEquals("km/h", record.speedSymbol());
        assertEquals("en", record.languageCode());
    }

    @Test
    void returnsLocalizedNamesForJapanese() throws Exception {
        dao.saveSpeed(10.0, 21, 30.0, 31, 20.0, 11, "ja");

        SpeedRecord record = dao.findRecentSpeed(1, "ja").get(0);

        assertEquals("キロメートル", record.distanceName());
        assertEquals("分", record.timeName());
        assertEquals("キロメートル毎時", record.speedName());
    }

    @Test
    void returnsLocalizedNamesForArabic() throws Exception {
        dao.saveSpeed(10.0, 21, 30.0, 31, 20.0, 11, "ar");

        SpeedRecord record = dao.findRecentSpeed(1, "ar").get(0);

        assertEquals("كيلومتر", record.distanceName());
        assertEquals("دقيقة", record.timeName());
        assertEquals("كيلومتر في الساعة", record.speedName());
    }

    @Test
    void fallsBackToEnglishNames() throws Exception {
        dao.saveSpeed(10.0, 21, 30.0, 31, 20.0, 11, "fr");

        SpeedRecord record = dao.findRecentSpeed(1, "fr").get(0);

        assertEquals("Kilometre", record.distanceName());
        assertEquals("Minute", record.timeName());
        assertEquals("Kilometres per hour", record.speedName());
    }

    @Test
    void limitsAndOrdersRecentCalculations() throws Exception {
        dao.saveSpeed(1.0, 20, 1.0, 30, 1.0, 10, "en");
        dao.saveSpeed(2.0, 20, 1.0, 30, 2.0, 10, "en");
        dao.saveSpeed(3.0, 20, 1.0, 30, 3.0, 10, "en");

        List<SpeedRecord> records = dao.findRecentSpeed(2, "en");

        assertEquals(2, records.size());
        assertEquals(3.0, records.get(0).distanceValue());
        assertEquals(2.0, records.get(1).distanceValue());
        assertTrue(dao.findRecentSpeed(0, "en").isEmpty());
    }
}
