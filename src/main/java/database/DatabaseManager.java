package database;

import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DEFAULT_DB_URL =
            "jdbc:h2:./data/tempconverter;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        String url = System.getProperty("h2.url", DEFAULT_DB_URL);
        return DriverManager.getConnection(url, USER, PASS);
    }

    public static void initializeDatabase() {
        String url = System.getProperty("h2.url", DEFAULT_DB_URL);

        try {
            if (url.startsWith("jdbc:h2:./") || url.startsWith("jdbc:h2:file:")) {
                Files.createDirectories(Path.of("data"));
            }

            try (Connection connection = getConnection();
                 InputStream input = DatabaseManager.class.getResourceAsStream("/init.sql")) {

                if (input == null) {
                    throw new IllegalStateException("Missing classpath resource: /init.sql");
                }

                try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    RunScript.execute(connection, reader);
                }
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Unable to initialize the database", exception);
        }
    }
}
