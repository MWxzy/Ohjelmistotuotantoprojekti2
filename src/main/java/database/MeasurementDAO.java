package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MeasurementDAO {

    private static final String INSERT_DISTANCE = """
            INSERT INTO distance_measurement (measurement_value, unit_id)
            VALUES (?, ?)
            """;

    private static final String INSERT_TIME = """
            INSERT INTO time_measurement (measurement_value, unit_id)
            VALUES (?, ?)
            """;

    private static final String INSERT_SPEED = """
            INSERT INTO speed_measurement
                (distance_id, time_id, speed_value, speed_unit_id, language_code)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SELECT_RECENT = """
            SELECT s.speed_id,
                   d.measurement_value AS distance_value,
                   COALESCE(dt.name, de.name) AS distance_name,
                   du.symbol AS distance_symbol,
                   t.measurement_value AS time_value,
                   COALESCE(tt.name, te.name) AS time_name,
                   tu.symbol AS time_symbol,
                   s.speed_value,
                   COALESCE(st.name, se.name) AS speed_name,
                   su.symbol AS speed_symbol,
                   s.language_code,
                   s.recorded_at
            FROM speed_measurement s
            JOIN distance_measurement d ON d.distance_id = s.distance_id
            JOIN time_measurement t ON t.time_id = s.time_id
            JOIN unit du ON du.unit_id = d.unit_id
            JOIN unit tu ON tu.unit_id = t.unit_id
            JOIN unit su ON su.unit_id = s.speed_unit_id
            LEFT JOIN unit_translation dt
                   ON dt.unit_id = du.unit_id AND dt.lang = ?
            LEFT JOIN unit_translation de
                   ON de.unit_id = du.unit_id AND de.lang = 'en'
            LEFT JOIN unit_translation tt
                   ON tt.unit_id = tu.unit_id AND tt.lang = ?
            LEFT JOIN unit_translation te
                   ON te.unit_id = tu.unit_id AND te.lang = 'en'
            LEFT JOIN unit_translation st
                   ON st.unit_id = su.unit_id AND st.lang = ?
            LEFT JOIN unit_translation se
                   ON se.unit_id = su.unit_id AND se.lang = 'en'
            ORDER BY s.recorded_at DESC, s.speed_id DESC
            LIMIT ?
            """;

    public long saveSpeed(
            double distanceValue,
            int distanceUnitId,
            double timeValue,
            int timeUnitId,
            double speedValue,
            int speedUnitId,
            String languageCode
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long distanceId = insertMeasurement(
                        connection,
                        INSERT_DISTANCE,
                        "distance_id",
                        distanceValue,
                        distanceUnitId
                );

                long timeId = insertMeasurement(
                        connection,
                        INSERT_TIME,
                        "time_id",
                        timeValue,
                        timeUnitId
                );

                long speedId;

                try (PreparedStatement statement = connection.prepareStatement(
                        INSERT_SPEED,
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    statement.setLong(1, distanceId);
                    statement.setLong(2, timeId);
                    statement.setDouble(3, speedValue);
                    statement.setInt(4, speedUnitId);
                    statement.setString(5, languageCode);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "No generated key returned for speed measurement."
                            );
                        }
                        speedId = keys.getLong(1);
                    }
                }

                connection.commit();
                return speedId;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<SpeedRecord> findRecentSpeed(int limit, String language) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_RECENT)) {
            statement.setString(1, language);
            statement.setString(2, language);
            statement.setString(3, language);
            statement.setInt(4, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<SpeedRecord> records = new ArrayList<>();
                while (resultSet.next()) {
                    records.add(new SpeedRecord(
                            resultSet.getLong("speed_id"),
                            resultSet.getDouble("distance_value"),
                            resultSet.getString("distance_name"),
                            resultSet.getString("distance_symbol"),
                            resultSet.getDouble("time_value"),
                            resultSet.getString("time_name"),
                            resultSet.getString("time_symbol"),
                            resultSet.getDouble("speed_value"),
                            resultSet.getString("speed_name"),
                            resultSet.getString("speed_symbol"),
                            resultSet.getString("language_code"),
                            resultSet.getTimestamp("recorded_at").toLocalDateTime()
                    ));
                }
                return records;
            }
        }
    }

    private long insertMeasurement(
            Connection connection,
            String sql,
            String idColumn,
            double value,
            int unitId
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                new String[]{idColumn}
        )) {
            statement.setDouble(1, value);
            statement.setInt(2, unitId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("No generated key returned for measurement.");
    }
}
