
package admain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

    // ============================
    // Helper: Reset static fields
    // ============================
    private void resetStaticFields() throws Exception {
        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, null);

        Field connField = database_connection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(null, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        resetStaticFields();
    }

    // ============================
    // Test 1: init() - dotenv null
    // يغطي: if (dotenv == null) → true
    // ============================
    @Test
    @DisplayName("init() should initialize dotenv when it is null")
    void testInit_WhenDotenvIsNull_ShouldInitialize() throws Exception {
        // Act
        database_connection.init();

        // Assert
        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        Object dotenvValue = dotenvField.get(null);

        assertNotNull(dotenvValue, "Dotenv should be initialized after init()");
    }

    // ============================
    // Test 2: init() - dotenv already set (idempotent)
    // يغطي: if (dotenv == null) → false
    // ============================
    @Test
    @DisplayName("init() should NOT reinitialize dotenv if already set")
    void testInit_WhenDotenvAlreadySet_ShouldNotReinitialize() throws Exception {
        // Arrange: set dotenv manually
        Dotenv mockDotenv = mock(Dotenv.class);
        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        // Act
        database_connection.init();

        // Assert: same instance (not replaced)
        Object dotenvAfter = dotenvField.get(null);
        assertSame(mockDotenv, dotenvAfter,
            "Dotenv should NOT be replaced if already initialized");
    }

    // ============================
    // Test 3: getConnection() - connection is null → creates new
    // يغطي: if (connection == null || connection.isClosed()) → true (null case)
    // ============================
    @Test
    @DisplayName("getConnection() should create new connection when connection is null")
    void testGetConnection_WhenConnectionIsNull_ShouldConnect() throws Exception {
        // Arrange
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        Connection mockConn = mock(Connection.class);

        try (MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""))
                .thenReturn(mockConn);

            // Act
            Connection result = database_connection.getConnection();

            // Assert
            assertNotNull(result, "Should return a valid connection");
            assertSame(mockConn, result);
            dmMock.verify(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""), times(1));
        }
    }

    // ============================
    // Test 4: getConnection() - connection closed → reconnect
    // يغطي: connection.isClosed() → true
    // ============================
    @Test
    @DisplayName("getConnection() should reconnect when connection is closed")
    void testGetConnection_WhenConnectionIsClosed_ShouldReconnect() throws Exception {
        // Arrange
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        // Set closed connection
        Connection closedConn = mock(Connection.class);
        when(closedConn.isClosed()).thenReturn(true);

        Field connField = database_connection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(null, closedConn);

        Connection newConn = mock(Connection.class);

        try (MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""))
                .thenReturn(newConn);

            // Act
            Connection result = database_connection.getConnection();

            // Assert
            assertSame(newConn, result, "Should return fresh connection after reconnect");
            dmMock.verify(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""), times(1));
        }
    }

    // ============================
    // Test 5: getConnection() - connection open → reuse
    // يغطي: if (connection == null || ...) → false (reuse path)
    // ============================
    @Test
    @DisplayName("getConnection() should reuse existing open connection")
    void testGetConnection_WhenConnectionIsOpen_ShouldReuse() throws Exception {
        // Arrange
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        Connection openConn = mock(Connection.class);
        when(openConn.isClosed()).thenReturn(false);

        Field connField = database_connection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(null, openConn);

        try (MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {
            // Act
            Connection result = database_connection.getConnection();

            // Assert
            assertSame(openConn, result, "Should reuse the existing open connection");
            dmMock.verify(() -> DriverManager.getConnection(
                anyString(), anyString(), anyString()), never());
        }
    }

    // ============================
    // Test 6: getConnection() - SQLException → return null
    // يغطي: catch (SQLException e) block
    // ============================
    @Test
    @DisplayName("getConnection() should return null and log error on SQLException")
    void testGetConnection_WhenSQLException_ShouldReturnNull() throws Exception {
        // Arrange
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        try (MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(
                anyString(), anyString(), anyString()))
                .thenThrow(new SQLException("Connection refused"));

            // Act
            Connection result = database_connection.getConnection();

            // Assert
            assertNull(result, "Should return null when SQLException occurs");
        }
    }
}
