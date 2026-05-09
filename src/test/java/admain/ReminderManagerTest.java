package admain;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
class ReminderManagerTest {

    @Mock
    private AppointmentRepository_y appointmentRepository;

    @Mock
    private NotificationService_y notificationService;

    @InjectMocks
    private ReminderManager_y reminderManager;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

@Test
void shouldNotSendDuplicateTenMinuteReminder() throws Exception {
    Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
    session_y.currentUser = user;

    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusMinutes(10);
    TimeSlot slot = new TimeSlot(1, start, start.plusMinutes(30));
    Appointment appt = new Appointment(200, 1, 1, slot, 1,
            AppointmentStatus_y.CONFIRMED, AppointmentType_y.GENERAL);

    when(appointmentRepository.getUserUpcomingAppointments(1))
            .thenReturn(List.of(appt));

    reminderManager.checkReminders();
    reminderManager.checkReminders();

    verify(notificationService, times(1))
            .sendReminder(eq("test@test.com"), contains("10 minutes"));
}

// exception في الـ catch
@Test
void shouldHandleExceptionGracefully() throws Exception {
    Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
    session_y.currentUser = user;

    when(appointmentRepository.getUserUpcomingAppointments(1))
            .thenThrow(new RuntimeException("DB error"));

    assertDoesNotThrow(() -> reminderManager.checkReminders());
    verifyNoInteractions(notificationService);
}
    // 🔥 Test 1: إشعار قبل ساعة
    @Test
    void shouldSendReminderOneHourBefore() {

        // إعداد المستخدم
        Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
        session_y.currentUser = user;

        // موعد بعد 60 دقيقة
        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusMinutes(60);
        TimeSlot slot = new TimeSlot(1, start, start.plusMinutes(30));

        Appointment appt = new Appointment(100, 1, 1, slot, 1,
                AppointmentStatus_y.CONFIRMED, AppointmentType_y.GENERAL);

        try {
			when(appointmentRepository.getUserUpcomingAppointments(1))
			        .thenReturn(List.of(appt));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // تنفيذ
        reminderManager.checkReminders();

        // تحقق
        verify(notificationService, times(1))
                .sendReminder(eq("test@test.com"), contains("1 hour"));
    }

  @Test
void shouldSendReminderTenMinutesBefore() throws Exception {
    Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
    session_y.currentUser = user;

    ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusMinutes(5);
    TimeSlot slot = new TimeSlot(1, start, start.plusMinutes(30));
    Appointment appt = new Appointment(101, 1, 1, slot, 1,
            AppointmentStatus_y.CONFIRMED, AppointmentType_y.GENERAL);

    when(appointmentRepository.getUserUpcomingAppointments(1))
            .thenReturn(List.of(appt));

    reminderManager.checkReminders();

    verify(notificationService, times(1))
            .sendReminder(eq("test@test.com"), contains("10 minutes"));
}
    @Test
    void shouldNotSendReminderIfFar() {

        Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
        session_y.currentUser = user;

        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusHours(5);
        TimeSlot slot = new TimeSlot(1, start, start.plusMinutes(30));

        Appointment appt = new Appointment(102, 1, 1, slot, 1,
                AppointmentStatus_y.CONFIRMED, AppointmentType_y.GENERAL);

        try {
			when(appointmentRepository.getUserUpcomingAppointments(1))
			        .thenReturn(List.of(appt));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        reminderManager.checkReminders();

        verify(notificationService, never()).sendReminder(any(), any());
    }

    // 🔥 Test 4: لا يكرر الإشعار
    @Test
    void shouldNotSendDuplicateReminder() {

        Account_y user = new Account_y(1, "user", "hash", "test@test.com", Role_y.USER);
        session_y.currentUser = user;

        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Asia/Hebron")).plusMinutes(60);
        TimeSlot slot = new TimeSlot(1, start, start.plusMinutes(30));

        Appointment appt = new Appointment(103, 1, 1, slot, 1,
                AppointmentStatus_y.CONFIRMED, AppointmentType_y.GENERAL);

        try {
			when(appointmentRepository.getUserUpcomingAppointments(1))
			        .thenReturn(List.of(appt));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // أول مرة
        reminderManager.checkReminders();

        // ثاني مرة
        reminderManager.checkReminders();

        verify(notificationService, times(1)).sendReminder(any(), any());
    }

    // 🔥 Test 5: لا يعمل بدون user
    @Test
    void shouldNotRunIfUserNotLoggedIn() {

        session_y.currentUser = null;

        reminderManager.checkReminders();

        verifyNoInteractions(notificationService);
    }
}
