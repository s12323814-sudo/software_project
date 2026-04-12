
    package admain;

    import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.*;

    import static org.junit.jupiter.api.Assertions.*;
    import static org.mockito.Mockito.*;

    class AuthServiceTest {

        @Mock
        private AccountRepository_y repo;

        @InjectMocks
        private authService_y authService;

        @BeforeEach
        void setup() {
            MockitoAnnotations.openMocks(this);
        }
        @Test
        void testLogin_wrongPassword() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            String hashed = BCrypt.hashpw("123456", BCrypt.gensalt());
            Account_y acc = new Account_y(1, "user", hashed, "a@test.com", Role_y.USER);

            when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

            session_y result = service.login("user", "wrong");

            assertNull(result);
        }@Test
        void testLogin_userNotFound() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            when(repo.findByUsernameOrEmail("x")).thenReturn(null);

            session_y result = service.login("x", "123");

            assertNull(result);
        }@Test
        void testLogin_nullInput() {
            authService_y service = new authService_y(mock(AccountRepository_y.class));

            assertNull(service.login(null, "123"));
            assertNull(service.login("user", null));
        }@Test
        void testLogin_nullHash() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            Account_y acc = mock(Account_y.class);
            when(acc.getPasswordHash()).thenReturn(null);

            when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

            assertThrows(RuntimeException.class,
                    () -> service.login("user", "123"));
        }@Test
        void testRegister_success() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            when(repo.usernameExists("user")).thenReturn(false);
            when(repo.emailExists("a@test.com")).thenReturn(false);

            Account_y saved = new Account_y(1, "user", "hash", "a@test.com", Role_y.USER);
            when(repo.save(anyString(), anyString(), anyString(), any())).thenReturn(saved);

            Account_y result = service.register("user", "123456", "a@test.com", Role_y.USER);

            assertNotNull(result);
        }@Test
        void testRegister_usernameExists() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            when(repo.usernameExists("user")).thenReturn(true);

            Account_y result = service.register("user", "123456", "a@test.com", Role_y.USER);

            assertNull(result);
        }@Test
        void testRegister_weakPassword() {
            authService_y service = new authService_y(mock(AccountRepository_y.class));

            Account_y result = service.register("user", "123", "a@test.com", Role_y.USER);

            assertNull(result);
        }
        // ================= LOGIN =================
        @Test
        void testLogin_success() {
            AccountRepository_y repo = mock(AccountRepository_y.class);
            authService_y service = new authService_y(repo);

            String hashed = BCrypt.hashpw("123456", BCrypt.gensalt());
            Account_y acc = new Account_y(1, "user", hashed, "a@test.com", Role_y.USER);

            when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

            session_y result = service.login("user", "123456");

            assertNotNull(result);
        }
        @Test
        void shouldLoginSuccessfully() {
            String password = "123456";
            String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

            Account_y acc = new Account_y(1, "user", hash, "test@test.com", Role_y.USER);

            when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

            session_y session = authService.login("user", password);

            assertNotNull(session);
            assertEquals("user", session.getAccount().getUsername());
        }

        @Test
        void shouldFailLogin_WhenPasswordIncorrect() {
            Account_y acc = new Account_y(1, "user",
                    org.mindrot.jbcrypt.BCrypt.hashpw("correct", org.mindrot.jbcrypt.BCrypt.gensalt()),
                    "test@test.com", Role_y.USER);

            when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

            assertNull(authService.login("user", "wrong"));
        }

        @Test
        void shouldFailLogin_WhenUserNotFound() {
            when(repo.findByUsernameOrEmail("user")).thenReturn(null);

            assertNull(authService.login("user", "123456"));
        }

        @Test
        void shouldFailLogin_WhenInputEmpty() {
            assertNull(authService.login("", "123"));
            assertNull(authService.login("user", ""));
        }

        // ================= REGISTER =================

        @Test
        void shouldRegisterSuccessfully() {
            when(repo.usernameExists("user")).thenReturn(false);
            when(repo.emailExists("test@test.com")).thenReturn(false);

            when(repo.save(any(), any(), any(), any()))
                    .thenReturn(new Account_y(1, "user", "hash", "test@test.com", Role_y.USER));

            Account_y result = authService.register("user", "123456", "test@test.com",  Role_y.USER);

            assertNotNull(result);
        }

        @Test
        void shouldFailRegister_WhenUsernameExists() {
            when(repo.usernameExists("user")).thenReturn(true);

            assertNull(authService.register("user", "123456", "test@test.com",  Role_y.USER));
        }

        @Test
        void shouldFailRegister_WhenEmailExists() {
            when(repo.usernameExists("user")).thenReturn(false);
            when(repo.emailExists("test@test.com")).thenReturn(true);

            assertNull(authService.register("user", "123456", "test@test.com",  Role_y.USER));
        }

        @Test
        void shouldFailRegister_WhenWeakPassword() {
            assertNull(authService.register("user", "123", "test@test.com", Role_y.USER));
        }

        @Test
        void shouldFailRegister_WhenInvalidEmail() {
            assertNull(authService.register("user", "123456", "wrong",  Role_y.USER));
        }

        // ================= PASSWORD =================

        @Test
        void shouldUpdatePasswordSuccessfully() {
            when(repo.updatePassword(eq("test@test.com"), any()))
                    .thenReturn(true);

            assertTrue(authService.updatePassword("test@test.com", "123456"));
        }

        @Test
        void shouldFailUpdatePassword_WhenWeak() {
            assertFalse(authService.updatePassword("test@test.com", "123"));
        }
    
    @Test
   
    void shouldHandleNullPasswordHash() {

        Account_y acc = mock(Account_y.class);

        when(acc.getPasswordHash()).thenReturn(null);
        when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            authService.login("user", "123");
        });

        assertEquals("Password hash is null", ex.getMessage());
    }
    @Test
    void shouldHandleDatabaseException() {
        when(repo.findByUsernameOrEmail(any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            authService.login("user", "123");
        });
    }
    @Test
    void sessionShouldContainCorrectUser() {
        String password = "123456";
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());

        Account_y acc = new Account_y(1, "user", hash, "test@test.com", Role_y.USER);

        when(repo.findByUsernameOrEmail("user")).thenReturn(acc);

        session_y session = authService.login("user", password);

        assertNotNull(session.getAccount());
        assertEquals(Role_y.USER, session.getAccount().getRole());
    }
    }
