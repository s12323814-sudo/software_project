package admain;

import java.sql.*;

/**
 * كلاس مسؤول عن التعامل مع قاعدة البيانات الخاصة بالحسابات (accounts).
 *
 * <p>يوفر هذا الكلاس عمليات CRUD الأساسية مثل:</p>
 * <ul>
 *   <li>البحث عن حساب باستخدام اسم المستخدم أو الإيميل</li>
 *   <li>التحقق من وجود اسم مستخدم أو إيميل</li>
 *   <li>إضافة حساب جديد</li>
 *   <li>تحديث كلمة المرور</li>
 * </ul>
 *
 * <p>يستخدم JDBC للاتصال بقاعدة البيانات من خلال database_connection.</p>
 */
public class AccountRepository_y {

    /**
     * البحث عن حساب باستخدام اسم المستخدم أو البريد الإلكتروني.
     *
     * @param input اسم المستخدم أو الإيميل
     * @return كائن Account_y إذا وُجد، أو null إذا لم يوجد
     */
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
            e.printStackTrace();
        }

        return null;
    }

    /**
     * البحث عن حساب باستخدام البريد الإلكتروني فقط.
     *
     * @param email البريد الإلكتروني
     * @return كائن Account_y إذا وُجد، أو null إذا لم يوجد
     */
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
            e.printStackTrace();
        }

        return null;
    }

    /**
     * التحقق إذا كان اسم المستخدم موجود مسبقًا.
     *
     * @param username اسم المستخدم
     * @return true إذا كان موجود، false إذا غير موجود
     */
    public boolean usernameExists(String username) {

        String sql = "SELECT 1 FROM accounts WHERE username = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.error("Error fetching account from database", e);
        }

        return false;
    }

    /**
     * التحقق إذا كان البريد الإلكتروني موجود مسبقًا.
     *
     * @param email البريد الإلكتروني
     * @return true إذا كان موجود، false إذا غير موجود
     */
    public boolean emailExists(String email) {

        String sql = "SELECT 1 FROM accounts WHERE email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.error("Error fetching account from database", e);
        }

        return false;
    }

    /**
     * حفظ حساب جديد في قاعدة البيانات.
     *
     * @param username اسم المستخدم
     * @param passwordHash كلمة المرور المشفرة
     * @param email البريد الإلكتروني
     * @param role دور المستخدم
     * @return كائن Account_y الجديد إذا تم الحفظ، أو null عند الفشل
     */
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
        logger.error("Error fetching account from database", e);
        }

        return null;
    }

    /**
     * تحديث كلمة المرور لحساب معين باستخدام الإيميل.
     *
     * @param email البريد الإلكتروني
     * @param passwordHash كلمة المرور الجديدة (مشفر)
     * @return true إذا تم التحديث بنجاح، false إذا فشل
     */
    public boolean updatePassword(String email, String passwordHash) {

        String sql = "UPDATE accounts SET password_hash = ? WHERE email = ?";

        try (Connection conn = database_connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, email);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
    logger.error("Database error while fetching account", e); 
        }

        return false;
    }

    /**
     * تحويل نتيجة الاستعلام (ResultSet) إلى كائن Account_y.
     *
     * @param rs نتيجة الاستعلام
     * @return كائن Account_y
     * @throws SQLException في حال حدوث خطأ أثناء القراءة
     */
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
