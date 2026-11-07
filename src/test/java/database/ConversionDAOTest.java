package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversionDAOTest {

    private ConversionDAO dao;

    @BeforeEach
    void setUp() {
        System.setProperty("h2.url",
                "jdbc:h2:mem:conversions" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        DatabaseManager.initializeDatabase();
        dao = new ConversionDAO();
    }

    @AfterEach
    void clearDatabaseOverride() {
        System.clearProperty("h2.url");
    }

    @Test
    void savesAndFindsRecentConversions() throws Exception {
        long first = dao.save(100.0, 1, 2, 212.0, "en");
        long second = dao.save(32.0, 2, 1, 0.0, "en");

        assertTrue(first > 0);
        assertTrue(second > first);

        List<ConversionRecord> records = dao.findRecent(5, "en");

        assertEquals(2, records.size());

        ConversionRecord latest = records.get(0);
        assertEquals(32.0, latest.inputValue());
        assertEquals("Fahrenheit", latest.fromName());
        assertEquals("°F", latest.fromSymbol());
        assertEquals(0.0, latest.resultValue());
        assertEquals("Celsius", latest.toName());
        assertEquals("°C", latest.toSymbol());
        assertEquals("en", latest.languageCode());
    }

    @Test
    void limitsRecentConversions() throws Exception {
        dao.save(1.0, 1, 2, 33.8, "en");
        dao.save(2.0, 1, 2, 35.6, "en");
        dao.save(3.0, 1, 2, 37.4, "en");

        assertEquals(1, dao.findRecent(1, "en").size());
        assertEquals(2, dao.findRecent(2, "en").size());
        assertTrue(dao.findRecent(0, "en").isEmpty());
    }

    @Test
    void returnsLocalizedUnitNamesForJapanese() throws Exception {
        dao.save(100.0, 1, 2, 212.0, "ja");

        ConversionRecord record = dao.findRecent(1, "ja").get(0);

        assertEquals("摂氏", record.fromName());
        assertEquals("華氏", record.toName());
    }

    @Test
    void returnsLocalizedUnitNamesForArabic() throws Exception {
        dao.save(100.0, 1, 2, 212.0, "ar");

        ConversionRecord record = dao.findRecent(1, "ar").get(0);

        assertEquals("مئوية", record.fromName());
        assertEquals("فهرنهايت", record.toName());
    }

    @Test
    void fallsBackToEnglishUnitNames() throws Exception {
        dao.save(100.0, 1, 2, 212.0, "fr");

        ConversionRecord record = dao.findRecent(1, "fr").get(0);

        assertEquals("Celsius", record.fromName());
        assertEquals("Fahrenheit", record.toName());
    }
}
