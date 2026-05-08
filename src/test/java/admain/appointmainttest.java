package admain;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class appointmenttest {


private static Connection conn;
private AppointmentService service;
private SlotService_y mockSlotService;
private scheduleRepository mockRepo;


@BeforeAll
static void setupDatabase() throws SQLException {
    conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    try (Statement st = conn.createStatement()) {
      
        st.execute("""
            CREATE TABLE appointment_slot (
                slot_id INT PRIMARY KEY AUTO_INCREMENT,
                slot_date DATE,
                slot_start_time TIME,
                slot_end_time TIME,
                max_capacity INT,
                booked_count INT DEFAULT 0
            )
        """);
      
        st.execute("""
            CREATE TABLE appointments (
                id INT PRIMARY KEY AUTO_INCREMENT,
                account_id INT,
                slot_id INT,
                start_time TIMESTAMP,
                end_time TIMESTAMP,
                duration INT,
                participants INT,
                status VARCHAR(20),
                type VARCHAR(20)
            )
        """);
    }
}

@BeforeEach
void setup() throws SQLException {
    mockSlotService = Mockito.mock(SlotService_y.class);
    mockRepo = Mockito.mock(scheduleRepository.class);
    service = new AppointmentService(conn, mockSlotService, mockRepo);

   
    try (Statement st = conn.createStatement()) {
        st.execute("DELETE FROM appointments");
        st.execute("DELETE FROM appointment_slot");
        st.execute("ALTER TABLE appointment_slot ALTER COLUMN slot_id RESTART WITH 1");
    }
}

private int insertSlot(LocalDate date, LocalTime start, LocalTime end,
                        int capacity, int booked) throws SQLException {
    String sql = "INSERT INTO appointment_slot (slot_date, slot_start_time, slot_end_time, max_capacity, booked_count) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setDate(1, Date.valueOf(date));
        ps.setTime(2, Time.valueOf(start));
        ps.setTime(3, Time.valueOf(end));
        ps.setInt(4, capacity);
        ps.setInt(5, booked);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
}


@Test
void testBookAppointment_Success() throws SQLException {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    int slotId = insertSlot(tomorrow, LocalTime.of(9, 0), LocalTime.of(10, 0), 5, 0);

    assertDoesNotThrow(() ->
        service.bookAppointment(1, slotId, 2, AppointmentType_y.GROUP)
    );

   
    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT booked_count FROM appointment_slot WHERE slot_id = ?")) {
        ps.setInt(1, slotId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        assertEquals(2, rs.getInt("booked_count"));
    }
}


@Test
void testBookAppointment_SlotNotFound() {
    SQLException ex = assertThrows(SQLException.class, () ->
        service.bookAppointment(1, 9999, 1, AppointmentType_y.INDIVIDUAL)
    );
    assertEquals("Slot not found", ex.getMessage());
}


@Test
void testBookAppointment_PastSlot() throws SQLException {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    int slotId = insertSlot(yesterday, LocalTime.of(9, 0), LocalTime.of(10, 0), 5, 0);

    SQLException ex = assertThrows(SQLException.class, () ->
        service.bookAppointment(1, slotId, 1, AppointmentType_y.INDIVIDUAL)
    );
    assertEquals("Cannot book a past slot", ex.getMessage());
}


@Test
void testBookAppointment_NotEnoughCapacity() throws SQLException {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    int slotId = insertSlot(tomorrow, LocalTime.of(9, 0), LocalTime.of(10, 0), 2, 2); // full

    SQLException ex = assertThrows(SQLException.class, () ->
        service.bookAppointment(1, slotId, 1, AppointmentType_y.INDIVIDUAL)
    );
    assertEquals("Not enough capacity for this slot", ex.getMessage());
}


@Test
void testBookAppointment_DurationTooShort() throws SQLException {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    int slotId = insertSlot(tomorrow, LocalTime.of(9, 0), LocalTime.of(9, 20), 5, 0); // 20 دقيقة

    assertThrows(IllegalArgumentException.class, () ->
        service.bookAppointment(1, slotId, 1, AppointmentType_y.INDIVIDUAL)
    );
}


@Test
void testBookAppointment_DurationTooLong() throws SQLException {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    int slotId = insertSlot(tomorrow, LocalTime.of(9, 0), LocalTime.of(12, 0), 5, 0); // 180 دقيقة

    assertThrows(IllegalArgumentException.class, () ->
        service.bookAppointment(1, slotId, 1, AppointmentType_y.INDIVIDUAL)
    );
}


@Test
void testGetUserAppointments() throws SQLException {
    when(mockRepo.getAppointments(1)).thenReturn(List.of());
    List<Appointment> result = service.getUserAppointments(1);
    assertNotNull(result);
    verify(mockRepo, times(1)).getAppointments(1);
}


@Test
void testGetAvailableSlots() {
    when(mockSlotService.getAvailableSlots()).thenReturn(List.of());
    List<AppointmentSlot_y> slots = service.getAvailableSlots();
    assertNotNull(slots);
    verify(mockSlotService, times(1)).getAvailableSlots();
}

@Test
void testAddSlot() {
    LocalDateTime start = LocalDateTime.now().plusDays(1);
    LocalDateTime end = start.plusHours(1);
    doNothing().when(mockSlotService).addSlot(any(), any(), any(), anyInt(), anyInt());
    assertDoesNotThrow(() -> service.addSlot(start, end, 5, 1));
}


@Test
void testBookAppointment_UrgentType() throws SQLException {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    int slotId = insertSlot(tomorrow, LocalTime.of(10, 0), LocalTime.of(11, 0), 5, 0);

    assertDoesNotThrow(() ->
        service.bookAppointment(1, slotId, 1, AppointmentType_y.URGENT)
    );
}

@AfterAll
static void teardown() throws SQLException {
    conn.close();
}


}