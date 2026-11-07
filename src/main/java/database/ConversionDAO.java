package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConversionDAO {

    private static final String INSERT = """
            INSERT INTO conversion_history
                (input_value, from_unit_id, to_unit_id, result_value, language_code)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SELECT_RECENT = """
            SELECT c.conversion_id,
                   c.input_value,
                   COALESCE(ft.name, fe.name) AS from_name,
                   fu.symbol AS from_symbol,
                   c.result_value,
                   COALESCE(tt.name, te.name) AS to_name,
                   tu.symbol AS to_symbol,
                   c.language_code,
                   c.created_at
            FROM conversion_history c
            JOIN unit fu ON fu.unit_id = c.from_unit_id
            JOIN unit tu ON tu.unit_id = c.to_unit_id
            LEFT JOIN unit_translation ft
                   ON ft.unit_id = fu.unit_id AND ft.lang = ?
            LEFT JOIN unit_translation fe
                   ON fe.unit_id = fu.unit_id AND fe.lang = 'en'
            LEFT JOIN unit_translation tt
                   ON tt.unit_id = tu.unit_id AND tt.lang = ?
            LEFT JOIN unit_translation te
                   ON te.unit_id = tu.unit_id AND te.lang = 'en'
            ORDER BY c.created_at DESC, c.conversion_id DESC
            LIMIT ?
            """;

    public long save(
            double inputValue,
            int fromUnitId,
            int toUnitId,
            double resultValue,
            String languageCode
    ) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT,
                     Statement.RETURN_GENERATED_KEYS
             )) {
            statement.setDouble(1, inputValue);
            statement.setInt(2, fromUnitId);
            statement.setInt(3, toUnitId);
            statement.setDouble(4, resultValue);
            statement.setString(5, languageCode);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("No generated key returned for conversion.");
        }
    }

    public List<ConversionRecord> findRecent(int limit, String language) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_RECENT)) {
            statement.setString(1, language);
            statement.setString(2, language);
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<ConversionRecord> records = new ArrayList<>();
                while (resultSet.next()) {
                    records.add(new ConversionRecord(
                            resultSet.getLong("conversion_id"),
                            resultSet.getDouble("input_value"),
                            resultSet.getString("from_name"),
                            resultSet.getString("from_symbol"),
                            resultSet.getDouble("result_value"),
                            resultSet.getString("to_name"),
                            resultSet.getString("to_symbol"),
                            resultSet.getString("language_code"),
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
                return records;
            }
        }
    }
}
