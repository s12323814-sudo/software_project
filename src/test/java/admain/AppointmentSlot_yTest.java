package admain;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppointmentSlot_yTest {

   
    @Test
    public void testConstructorAndGetters1() {

        Appointment appt = new Appointment(
                1,   // appointmentId
                10,  // userId
                5,   // slotId
                3,   // participants
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE
        );

        assertEquals(1, appt.getAppointmentId());
        assertEquals(10, appt.getUserId());
        assertEquals(5, appt.getSlotId());
        assertEquals(3, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
        assertEquals(AppointmentType_y.ONLINE, appt.getType());
    }@Test
void testIsSlotAvailable_returnsTrue() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt(1)).thenReturn(0); // مفيش حجز = متاح

    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, LocalDate.now(), LocalTime.of(10,0), 
        LocalTime.of(11,0), 5, 0, conn
    );

    assertTrue(slot.isSlotAvailableForResource(1, 1));
}

@Test
void testIsSlotAvailable_returnsFalse() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(true);
    when(rs.getInt(1)).thenReturn(1); // في حجز = مش متاح

    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, LocalDate.now(), LocalTime.of(10,0), 
        LocalTime.of(11,0), 5, 0, conn
    );

    assertFalse(slot.isSlotAvailableForResource(1, 1));
}

@Test
void testIsSlotAvailable_rsNextFalse() throws Exception {
    Connection conn = mock(Connection.class);
    PreparedStatement ps = mock(PreparedStatement.class);
    ResultSet rs = mock(ResultSet.class);

    when(conn.prepareStatement(anyString())).thenReturn(ps);
    when(ps.executeQuery()).thenReturn(rs);
    when(rs.next()).thenReturn(false); // ما رجع نتيجة

    AppointmentSlot_y slot = new AppointmentSlot_y(
        1, LocalDate.now(), LocalTime.of(10,0), 
        LocalTime.of(11,0), 5, 0, conn
    );

    assertFalse(slot.isSlotAvailableForResource(1, 1));
}
    @Test
    public void testToString() {

        Appointment appt = new Appointment(
                1,
                10,
                5,
                3,
                AppointmentStatus_y.CONFIRMED,
                AppointmentType_y.ONLINE
        );

        String result = appt.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("userId=10"));
        assertTrue(result.contains("slotId=5"));
        assertTrue(result.contains("participants=3"));
        assertTrue(result.contains("CONFIRMED"));
        assertTrue(result.contains("ONLINE"));
    }@Test
    void testBookedCount_zero() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                0
        );

        assertEquals(0, slot.getBookedCount());
        assertFalse(slot.isFull());
    }@Test
    void testSingleCapacitySlot() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                1,
                1
        );

        assertTrue(slot.isFull());
    }@Test
    void testToString_fullContent() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                99,
                LocalDate.of(2026, 12, 31),
                LocalTime.of(23, 0),
                LocalTime.of(23, 59),
                20,
                10
        );

        String result = slot.toString();

        assertTrue(result.contains("99"));
        assertTrue(result.contains("2026-12-31"));
        assertTrue(result.contains("23:00"));
        assertTrue(result.contains("23:59"));
    }@Test
    void testNullHandling_ifAny() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                0
        );

        assertNotNull(slot.toString());
    }@Test
    void testDataIntegrity() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(9, 0);

        AppointmentSlot_y slot = new AppointmentSlot_y(
                2, date, start, end, 10, 3
        );

        assertEquals(date, slot.getDate());
        assertEquals(start, slot.getStartTime());
        assertEquals(end, slot.getEndTime());
    }@Test
    void testToString_noNulls() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                5,
                LocalDate.of(2026, 6, 6),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                2
        );

        String result = slot.toString();

        assertFalse(result.contains("null"));
    }
    @Test
    void testExactlyFullBoundary() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                5
        );

        assertTrue(slot.isFull());
    }@Test
    void testOverCapacityData() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                7
        );

        assertTrue(slot.isFull()); // لازم تعتبرها full
    }@Test
    void testZonedDateTimeZoneCorrectness() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                1
        );

        assertEquals("Asia/Hebron", slot.getStartDateTime().getZone().getId());
    }@Test
    void testStartBeforeEnd() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertTrue(slot.getEndDateTime().isAfter(slot.getStartDateTime()));
    }@Test
    void testToString_full() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                99,
                LocalDate.of(2026, 12, 31),
                LocalTime.of(23, 0),
                LocalTime.of(23, 59),
                10,
                6
        );

        String result = slot.toString();

        assertTrue(result.contains("99"));
        assertTrue(result.contains("2026"));
        assertTrue(result.contains("Capacity"));
        assertTrue(result.contains("6/10"));
    }
    @Test
    void testIsFull_whenNotFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                2
        );

        assertFalse(slot.isFull());
    }@Test
    void testIsFull_whenExactFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                3,
                3
        );

        assertTrue(slot.isFull());
    }@Test
    void testIsFull_overCapacity() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                10
        );

        assertTrue(slot.isFull());
    }@Test
    void testZoneIsCorrect() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertEquals("Asia/Hebron", slot.getStartDateTime().getZone().getId());
    }@Test
    void testTimeOrderCorrect() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                5,
                1
        );

        assertTrue(slot.getEndDateTime().isAfter(slot.getStartDateTime()));
    }@Test
    void testSlotAvailable_exceptionPath() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                0
        );

        assertThrows(RuntimeException.class, () -> {
            slot.isSlotAvailableForResource(1, 1);
        });
    }@Test
    void testToString_fullCheck() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                7,
                LocalDate.of(2026, 5, 5),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                4
        );

        String result = slot.toString();

        assertTrue(result.contains("7"));
        assertTrue(result.contains("2026-05-05"));
        assertTrue(result.contains("10:00"));
        assertTrue(result.contains("4/10"));
    }
    @Test
    void testIsFull_edgeCases() {
        AppointmentSlot_y slot1 = new AppointmentSlot_y(
                1, LocalDate.now(), LocalTime.now(),
                LocalTime.now().plusHours(1), 5, 5
        );

        AppointmentSlot_y slot2 = new AppointmentSlot_y(
                2, LocalDate.now(), LocalTime.now(),
                LocalTime.now().plusHours(1), 5, 4
        );

        assertTrue(slot1.isFull());
        assertFalse(slot2.isFull());
    }@Test
    void testDateTimeConsistency() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1, date, start, end, 5, 2
        );

        assertEquals(date, slot.getStartDateTime().toLocalDate());
        assertEquals(start, slot.getStartDateTime().toLocalTime());

        assertEquals(date, slot.getEndDateTime().toLocalDate());
        assertEquals(end, slot.getEndDateTime().toLocalTime());
    }@Test
    void test_isSlotAvailableForResource_truePath() throws Exception {

        SlotRepository_y repo = mock(SlotRepository_y.class);
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                0
        );

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(toString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // 🔥 هذا أهم سطرين
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);


     
    }
    @Test
    void testToString_fullCheck1() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                10,
                LocalDate.of(2026, 3, 3),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                10,
                4
        );

        String result = slot.toString();

        assertTrue(result.contains("ID: 10"));
        assertTrue(result.contains("2026-03-03"));
        assertTrue(result.contains("08:00"));
        assertTrue(result.contains("09:00"));
        assertTrue(result.contains("4/10"));
    }@Test
    void testToString_notNull1() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                1
        );

        assertNotNull(slot.toString());
    }@Test
    void testToString_notNull() {

        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                1
        );

        assertNotNull(slot.toString());
    }@Test
    void testEmptySlotIsNotFull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10,
                0
        );

        assertFalse(slot.isFull());
    }
    @Test
    void testOneSpotLeft() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                4
        );

        assertFalse(slot.isFull());
    }@Test
    void testNullHandling_ifAny1() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                0
        );

        assertNotNull(slot.toString());
    }
    @Test
    public void testSetters() {

        Appointment appt = new Appointment(
                1, 10, 5, 2,
                AppointmentStatus_y.PENDING,
                AppointmentType_y.OFFLINE
        );

        appt.setParticipants(6);
        appt.setStatus(AppointmentStatus_y.CONFIRMED);

        assertEquals(6, appt.getParticipants());
        assertEquals(AppointmentStatus_y.CONFIRMED, appt.getStatus());
    }
    @Test
    void testConstructorAndGetters() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 1, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                5,
                2
        );

        assertEquals(1, slot.getId());
        assertEquals(LocalDate.of(2026, 1, 1), slot.getDate());
        assertEquals(LocalTime.of(10, 0), slot.getStartTime());
        assertEquals(LocalTime.of(11, 0), slot.getEndTime());
        assertEquals(5, slot.getMaxCapacity());
        assertEquals(2, slot.getBookedCount());
    }

    @Test
    void testIsFull_false() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                3
        );

        assertFalse(slot.isFull());
    }

    @Test
    void testIsFull_true() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                5
        );

        assertTrue(slot.isFull());
    }

    @Test
    void testGetStartDateTime_notNull() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertNotNull(slot.getStartDateTime());
    }

    @Test
    void testGetEndDateTime_afterStart() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.of(2026, 2, 2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                5,
                1
        );

        assertTrue(slot.getEndDateTime().isAfter(slot.getStartDateTime()));
    }

    @Test
    void testToString_containsData() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                10,
                LocalDate.of(2026, 3, 3),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                10,
                4
        );

        String result = slot.toString();

        assertTrue(result.contains("10"));
        assertTrue(result.contains("2026-03-03"));
        assertTrue(result.contains("08:00"));
    }

    @Test
    void testSlotAvailableForResource_exceptionPath() {
        AppointmentSlot_y slot = new AppointmentSlot_y(
                1,
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                5,
                0
        );

         assertThrows(RuntimeException.class, () -> {
            slot.isSlotAvailableForResource(1, 1);
        });
    }

    @Test
    void testSlotAvailableForResource_returnsTrue() throws Exception {

          SlotRepository_y repo = mock(SlotRepository_y.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
  when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

         when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0);

          YourService service = new YourService(repo);

        boolean result = service.isSlotAvailableForResource(1, 1);

        assertTrue(result);
    }
}
