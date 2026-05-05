package admain;

import java.sql.*;
import java.util.logging.Logger;

public class AccountRepository_y {

    private static final Logger logger =
            Logger.getLogger(AccountRepository_y.class.getName());

    public Account_y findByUsernameOrEmail(String input) {

        String sql = "SELECT * FROM accounts WHERE username = ? OR email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, input);
            stmt.setString(2, input);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching account from database: " + e.getMessage());
        }

        return null;
    }

    public Account_y findByEmail(String email) {

        String sql = "SELECT * FROM accounts WHERE email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error fetching account from database: " + e.getMessage());
        }

        return null;
    }

    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM accounts WHERE username = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.severe("Error checking username: " + e.getMessage());
        }

        return false;
    }

    public boolean emailExists(String email) {

        String sql = "SELECT 1 FROM accounts WHERE email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.severe("Error checking email: " + e.getMessage());
        }

        return false;
    }

    public Account_y save(String username, String passwordHash, String email, Role_y role) {

        String sql = "INSERT INTO accounts (username, password_hash, email, role) " +
                     "VALUES (?, ?, ?, ?) RETURNING account_id";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, email);
            stmt.setString(4, role.name());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("account_id");

                return new Account_y(
                        id,
                        username,
                        passwordHash,
                        email,
                        role
                );
            }

        } catch (SQLException e) {
            logger.severe("Error saving account: " + e.getMessage());
        }

        return null;
    }

    public boolean updatePassword(String email, String passwordHash) {

        String sql = "UPDATE accounts SET password_hash = ? WHERE email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, email);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.severe("Error updating password: " + e.getMessage());
        }

        return false;
    }

    private Account_y mapResultSetToAccount(ResultSet rs) throws SQLException {

        return new Account_y(
                rs.getInt("account_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                Role_y.fromString(rs.getString("role"))
        );
    }
}
