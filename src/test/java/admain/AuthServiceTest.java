package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

@ExtendWith(MockitoExtension.class)
class AuthServiceCleanTest {

    @Mock
    private AccountRepository_y repo;

    @InjectMocks
    private authService_y service;

    private Account_y mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = new Account_y(
                1,
                "user",
                BCrypt.hashpw("123456", BCrypt.gensalt()),
                "test@test.com",
                Role_y.USER
        );
    }

    // ================= LOGIN =================

    @Test
    void login_success() {
        when(repo.findByUsernameOrEmail("user")).thenReturn(mockAccount);

        session_y result = service.login("user", "123456");

        assertNotNull(result);
        assertEquals("user", result.getAccount().getUsername());
    }

    @Test
    void login_wrongPassword() {
        when(repo.findByUsernameOrEmail("user")).thenReturn(mockAccount);

        assertNull(service.login("user", "wrong"));
    }

    @Test
    void login_userNotFound() {
        when(repo.findByUsernameOrEmail("user")).thenReturn(null);

        assertNull(service.login("user", "123456"));
    }

    @Test
    void login_invalidInput() {
        assertNull(service.login(null, "123456"));
        assertNull(service.login("user", null));
        assertNull(service.login("", "123456"));
    }

    @Test
    void login_nullPasswordHash() {
        Account_y acc = mock(Account_y.class);
        when(acc.getPasswordHash()).thenReturn(null);

        when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

        assertThrows(RuntimeException.class,
                () -> service.login("user", "123"));
    }

    // ================= REGISTER =================

    @Test
    void register_success() {
        when(repo.usernameExists("newuser")).thenReturn(false);
        when(repo.emailExists("new@test.com")).thenReturn(false);

        when(repo.save(anyString(), anyString(), anyString(), any()))
                .thenReturn(mockAccount);

        Account_y result = service.register(
                "newuser",
                "123456",
                "new@test.com",
                Role_y.USER
        );

        assertNotNull(result);
    }

    @Test
    void register_usernameExists() {
        when(repo.usernameExists("user")).thenReturn(true);

        assertNull(service.register("user", "123456", "a@test.com", Role_y.USER));
    }

    @Test
    void register_emailExists() {
        when(repo.usernameExists("user")).thenReturn(false);
        when(repo.emailExists("test@test.com")).thenReturn(true);

        assertNull(service.register("user", "123456", "test@test.com", Role_y.USER));
    }

    @Test
    void register_weakPassword() {
        assertNull(service.register("user", "123", "test@test.com", Role_y.USER));
    }

    @Test
    void register_invalidEmail() {
        assertNull(service.register("user", "123456", "invalid", Role_y.USER));
    }

    // ================= PASSWORD =================

    @Test
    void updatePassword_success() {
        when(repo.updatePassword(eq("test@test.com"), anyString()))
                .thenReturn(true);

        assertTrue(service.updatePassword("test@test.com", "123456"));
    }

    @Test
    void updatePassword_weakPassword() {
        assertFalse(service.updatePassword("test@test.com", "123"));
    }

    // ================= EMAIL =================

    @Test
    void emailExists_true() {
        when(repo.findByEmail("test@test.com")).thenReturn(mockAccount);

        assertTrue(service.emailExists("test@test.com"));
    }

    @Test
    void emailExists_false() {
        when(repo.findByEmail("x@test.com")).thenReturn(null);

        assertFalse(service.emailExists("x@test.com"));
    }
@Test
void testIsSlotAvailableForResource_sqlException() throws Exception {
    SlotRepository_y mockRepo = mock(SlotRepository_y.class);
    Connection mockConn = mock(Connection.class);
    
    when(mockRepo.getConnection()).thenReturn(mockConn);
    when(mockConn.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
    
    YourService service = new YourService(mockRepo);
    
    assertThrows(RuntimeException.class, () -> 
        service.isSlotAvailableForResource(1, 1));
}
    // ================= GET ACCOUNT =================

    @Test
    void getAccountByEmail_success() {
        when(repo.findByEmail("test@test.com")).thenReturn(mockAccount);

        Account_y result = service.getAccountByEmail("test@test.com");

        assertNotNull(result);
    }
}
