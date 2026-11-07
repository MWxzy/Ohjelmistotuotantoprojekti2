package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UnitDAO {

    private static final String SELECT_UNITS = """
            SELECT u.unit_id,
                   u.unit_key,
                   u.category,
                   u.symbol,
                   u.factor_to_base,
                   u.offset_to_base,
                   COALESCE(t.name, e.name) AS name,
                   COALESCE(t.description, e.description) AS description
            FROM unit u
            LEFT JOIN unit_translation t
                   ON t.unit_id = u.unit_id AND t.lang = ?
            LEFT JOIN unit_translation e
                   ON e.unit_id = u.unit_id AND e.lang = 'en'
            """;

    public List<Unit> findByCategory(String category, String language) throws SQLException {
        String sql = SELECT_UNITS
                + " WHERE u.category = ?"
                + " ORDER BY u.display_order, u.unit_id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, language);
            statement.setString(2, category);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Unit> units = new ArrayList<>();
                while (resultSet.next()) {
                    units.add(map(resultSet));
                }
                return units;
            }
        }
    }

    public Unit findById(int id, String language) throws SQLException {
        String sql = SELECT_UNITS + " WHERE u.unit_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, language);
            statement.setInt(2, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    public Unit findByKey(String key, String language) throws SQLException {
        String sql = SELECT_UNITS + " WHERE u.unit_key = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, language);
            statement.setString(2, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        }
    }

    private Unit map(ResultSet resultSet) throws SQLException {
        return new Unit(
                resultSet.getInt("unit_id"),
                resultSet.getString("unit_key"),
                resultSet.getString("category"),
                resultSet.getString("symbol"),
                resultSet.getDouble("factor_to_base"),
                resultSet.getDouble("offset_to_base"),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
