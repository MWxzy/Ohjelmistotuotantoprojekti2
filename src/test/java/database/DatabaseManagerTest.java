package database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseManagerTest {

    @BeforeEach
    void useInMemoryDatabase() {
        System.setProperty("h2.url",
                "jdbc:h2:mem:dbmanager" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
    }

    @AfterEach
    void clearDatabaseOverride() {
        System.clearProperty("h2.url");
    }

    @Test
    void testGetConnection() throws SQLException {
        DatabaseManager.initializeDatabase();

        try (Connection connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void initializeCreatesLocalizedUnitData() throws SQLException {
        DatabaseManager.initializeDatabase();
        DatabaseManager.initializeDatabase();

        assertEquals(12, count("unit"));
        assertEquals(60, count("unit_translation"));
        assertEquals(0, count("conversion_history"));
        assertEquals(0, count("distance_measurement"));
        assertEquals(0, count("time_measurement"));
        assertEquals(0, count("speed_measurement"));
    }

    private int count(String table) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM " + table
             )) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
