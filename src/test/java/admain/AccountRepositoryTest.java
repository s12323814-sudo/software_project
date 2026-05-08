package admain;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountRepositoryTest {

    private AccountRepository_y repo;
    private static MockedStatic<database_connection> dbStaticMock;

    private Connection mockConn;
    private PreparedStatement mockStmt;
    private ResultSet mockRs;

    @BeforeAll
    static void beforeAll() {
        // Static mock لقاعدة البيانات مرة واحدة لكل الكلاس
        dbStaticMock = mockStatic(database_connection.class);
    }

    @AfterAll
    static void afterAll() {
        dbStaticMock.close();
    }

    @BeforeEach
    void setup() throws Exception {
        repo = new AccountRepository();

        // Mock objects
        mockConn = mock(Connection.class);
        mockStmt = mock(PreparedStatement.class);
        mockRs = mock(ResultSet.class);

        // كل استدعاء database_connection.getConnection() يرجع mockConn
        dbStaticMock.when(database_connection::getConnection).thenReturn(mockConn);

        // كل prepareStatement يرجع mockStmt
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);

        // executeQuery يرجع mockRs
        when(mockStmt.executeQuery()).thenReturn(mockRs);

        // executeUpdate يرجع 1 بشكل افتراضي
        when(mockStmt.executeUpdate()).thenReturn(1);
    }

    // ===== findByUsernameOrEmail =====
    @Test
    void testFindByUsernameOrEmail() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("account_id")).thenReturn(1);
        when(mockRs.getString("username")).thenReturn("user");
        when(mockRs.getString("password_hash")).thenReturn("hash");
        when(mockRs.getString("email")).thenReturn("test@test.com");
        when(mockRs.getString("role")).thenReturn("USER");

        Account_y acc = repo.findByUsernameOrEmail("user");
        assertNotNull(acc);
        assertEquals("user", acc.getUsername());
        assertEquals("hash", acc.getPasswordHash());
        assertEquals("test@test.com", acc.getEmail());
    }
@Test
void testFindByUsernameOrEmail_NotFound() throws SQLException {
    when(mockRs.next()).thenReturn(false); // مفيش نتائج
    Account_y acc = repo.findByUsernameOrEmail("nobody");
    assertNull(acc);
}

@Test
void testFindByEmail_NotFound() throws SQLException {
    when(mockRs.next()).thenReturn(false);
    Account_y acc = repo.findByEmail("ghost@test.com");
    assertNull(acc);
}

@Test
void testSave_WhenInsertFails_ReturnsNull() throws SQLException {
    when(mockRs.next()).thenReturn(false); // RETURNING ما رجّع id
    Account_y acc = repo.save("u", "h", "e@test.com", Role_y.USER);
    assertNull(acc);
}@Test
void testFindByUsernameOrEmail_SQLException() throws SQLException {
    when(mockStmt.executeQuery())
        .thenThrow(new SQLException("DB error"));
    Account_y acc = repo.findByUsernameOrEmail("user");
    assertNull(acc); // بيرجع null بدون exception
}

@Test
void testFindByEmail_SQLException() throws SQLException {
    when(mockStmt.executeQuery())
        .thenThrow(new SQLException("DB error"));
    Account_y acc = repo.findByEmail("test@test.com");
    assertNull(acc);
}

@Test
void testUsernameExists_SQLException() throws SQLException {
    when(mockStmt.executeQuery())
        .thenThrow(new SQLException("DB error"));
    assertFalse(repo.usernameExists("user")); // false عند الخطأ
}@Test
void testFindByEmail_AdminRole() throws SQLException {
    when(mockRs.next()).thenReturn(true);
    when(mockRs.getInt("account_id")).thenReturn(5);
    when(mockRs.getString("username")).thenReturn("admin");
    when(mockRs.getString("password_hash")).thenReturn("adminHash");
    when(mockRs.getString("email")).thenReturn("admin@test.com");
    when(mockRs.getString("role")).thenReturn("ADMIN");

    Account_y acc = repo.findByEmail("admin@test.com");
    assertNotNull(acc);
    assertEquals(Role_y.ADMIN, acc.getRole());
    assertEquals(5, acc.getAccountId());
}@Test
void testFindByUsernameOrEmail_SetsParams() throws SQLException {
    when(mockRs.next()).thenReturn(false);

    repo.findByUsernameOrEmail("testInput");

    // تأكد إن الباراميتر اتحط مرتين (username و email)
    verify(mockStmt).setString(1, "testInput");
    verify(mockStmt).setString(2, "testInput");
}

@Test
void testSave_WithAdminRole() throws SQLException {
    when(mockRs.next()).thenReturn(true);
    when(mockRs.getInt("account_id")).thenReturn(99);

    Account_y acc = repo.save("adminUser", "hash", "a@test.com", Role_y.ADMIN);
    assertNotNull(acc);
    assertEquals(Role_y.ADMIN, acc.getRole());

    // تأكد إن الـ role اتبعت للـ DB صح
    verify(mockStmt).setString(4, "ADMIN");
}

@Test
void testEmailExists_SQLException() throws SQLException {
    when(mockStmt.executeQuery())
        .thenThrow(new SQLException("DB error"));
    assertFalse(repo.emailExists("test@test.com"));
}

@Test
void testSave_SQLException() throws SQLException {
    when(mockStmt.executeQuery())
        .thenThrow(new SQLException("DB error"));
    Account_y acc = repo.save("u", "h", "e@test.com", Role_y.USER);
    assertNull(acc);
}

@Test
void testUpdatePassword_SQLException() throws SQLException {
    when(mockStmt.executeUpdate())
        .thenThrow(new SQLException("DB error"));
    assertFalse(repo.updatePassword("test@test.com", "hash"));
}
    // ===== findByEmail =====
    @Test
    void testFindByEmail() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("account_id")).thenReturn(2);
        when(mockRs.getString("username")).thenReturn("abc");
        when(mockRs.getString("password_hash")).thenReturn("hash2");
        when(mockRs.getString("email")).thenReturn("abc@test.com");
        when(mockRs.getString("role")).thenReturn("ADMIN");

        Account_y acc = repo.findByEmail("abc@test.com");
        assertNotNull(acc);
        assertEquals("abc", acc.getUsername());
        assertEquals("ADMIN", acc.getRole().name());
    }

    // ===== usernameExists =====
    @Test
    void testUsernameExists() throws SQLException {
        when(mockRs.next()).thenReturn(true); // موجود
        assertTrue(repo.usernameExists("user"));

        when(mockRs.next()).thenReturn(false); // غير موجود
        assertFalse(repo.usernameExists("nouser"));
    }

    // ===== emailExists =====
    @Test
    void testEmailExists() throws SQLException {
        when(mockRs.next()).thenReturn(true); // موجود
        assertTrue(repo.emailExists("test@test.com"));

        when(mockRs.next()).thenReturn(false); // غير موجود
        assertFalse(repo.emailExists("no@test.com"));
    }

    // ===== save =====
    @Test
    void testSave() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt("account_id")).thenReturn(10);

        Account_y acc = repo.save("newuser", "newhash", "new@test.com",  Role_y.USER);
        assertNotNull(acc);
        assertEquals(10, acc.getAccountId());
        assertEquals("newuser", acc.getUsername());
        assertEquals("newhash", acc.getPasswordHash());
        assertEquals("USER", acc.getRole().name());
    }

    // ===== updatePassword =====
    @Test
    void testUpdatePassword() throws SQLException {
        // executeUpdate يرجع > 0 يعني نجح
        when(mockStmt.executeUpdate()).thenReturn(1);
        assertTrue(repo.updatePassword("test@test.com", "newhash"));

        // executeUpdate يرجع 0 يعني فشل
        when(mockStmt.executeUpdate()).thenReturn(0);
        assertFalse(repo.updatePassword("test@test.com", "failhash"));
    }
}
