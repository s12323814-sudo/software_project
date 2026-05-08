package admain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConnectionTest {

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

    @Test
    @DisplayName("init() should initialize dotenv when it is null")
    void testInit_WhenDotenvIsNull_ShouldInitialize() throws Exception {
        database_connection.init();

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        Object dotenvValue = dotenvField.get(null);

        assertNotNull(dotenvValue, "Dotenv should be initialized after init()");
    }

    @Test
    @DisplayName("init() should NOT reinitialize dotenv if already set")
    void testInit_WhenDotenvAlreadySet_ShouldNotReinitialize() throws Exception {
        Dotenv mockDotenv = mock(Dotenv.class);
        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        database_connection.init();

        Object dotenvAfter = dotenvField.get(null);
        assertSame(mockDotenv, dotenvAfter,
            "Dotenv should NOT be replaced if already initialized");
    }

    @Test
    @DisplayName("getConnection() should create new connection when connection is null")
    void testGetConnection_WhenConnectionIsNull_ShouldConnect() throws Exception {
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

            Connection result = database_connection.getConnection();

            assertNotNull(result);
            assertSame(mockConn, result);
            dmMock.verify(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""), times(1));
        }
    }

    @Test
    @DisplayName("getConnection() should reconnect when connection is closed")
    void testGetConnection_WhenConnectionIsClosed_ShouldReconnect() throws Exception {
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

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

            Connection result = database_connection.getConnection();

            assertSame(newConn, result);
            dmMock.verify(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""), times(1));
        }
    }

    @Test
    @DisplayName("getConnection() should reuse existing open connection")
    void testGetConnection_WhenConnectionIsOpen_ShouldReuse() throws Exception {
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
            Connection result = database_connection.getConnection();

            assertSame(openConn, result);
            dmMock.verify(() -> DriverManager.getConnection(
                anyString(), anyString(), anyString()), never());
        }
    }

    @Test
    @DisplayName("getConnection() should return null and log error on SQLException")
    void testGetConnection_WhenSQLException_ShouldReturnNull() throws Exception {
        Dotenv mockDotenv = mock(Dotenv.class);
        when(mockDotenv.get("DB_URL")).thenReturn("jdbc:h2:mem:testdb");
        when(mockDotenv.get("DB_USER")).thenReturn("sa");
        when(mockDotenv.get("DB_PASS")).thenReturn("");

        Field dotenvField = database_connection.class.getDeclaredField("dotenv");
        dotenvField.setAccessible(true);
        dotenvField.set(null, mockDotenv);

        try (MockedStatic<DriverManager> dmMock = mockStatic(DriverManager.class)) {
            dmMock.when(() -> DriverManager.getConnection(
                "jdbc:h2:mem:testdb", "sa", ""))
                .thenThrow(new SQLException("Connection refused"));

            Connection result = database_connection.getConnection();

            assertNull(result);
        }
    }
}
