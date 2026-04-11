package admain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class AppointmentRepositoryTest4{

    private AppointmentRepository_y repo;
    private SlotRepository_y slotRepo;

    @BeforeEach
    void setUp() {
        slotRepo = mock(SlotRepository_y.class);
        repo = new AppointmentRepository_y(slotRepo);
    }

    // ====================== book() happy path ======================
    @Test
    void testBookSuccess() throws Exception {
        int slotId = 1;
        int userId = 10;

        // Mock slot
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slotRepo.findById(slotId)).thenReturn(slot);

        // Mock Connection
        Connection conn = mock(Connection.class);
        PreparedStatement psInsert = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(psUpdate.executeUpdate()).thenReturn(1);

        when(conn.prepareStatement(contains("INSERT"))).thenReturn(psInsert);
        when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

           
        }
    

    // ====================== book() errors ======================
    @Test
    void testBookSlotNotFound() {
        when(slotRepo.findById(1)).thenReturn(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> repo.book(10, 1, 1, AppointmentType_y.ONLINE));
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void testBookSlotEnded() {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2000, 1, 1)); // ماضي
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(0);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slotRepo.findById(1)).thenReturn(slot);

           
    }

    @Test
    void testBookSlotOverCapacity() {
        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getDate()).thenReturn(LocalDate.of(2025, 1, 1));
        when(slot.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(slot.getEndTime()).thenReturn(LocalTime.of(11, 0));
        when(slot.getBookedCount()).thenReturn(4);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slotRepo.findById(1)).thenReturn(slot);

        assertThrows(IllegalStateException.class,
                () -> repo.book(10, 1, 2, AppointmentType_y.ONLINE));
    }

    // ====================== cancel() ======================
    @Test
    void testCancelSuccess() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psDelete = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            // SELECT
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("slot_id")).thenReturn(1);
            when(rs.getInt("participants")).thenReturn(2);

            // DELETE نجاح
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(psDelete);
            when(psDelete.executeUpdate()).thenReturn(1);

            // UPDATE نجاح
            when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);
            when(psUpdate.executeUpdate()).thenReturn(1);

            boolean result = repo.cancel(10, 1);

            assertTrue(result);
        }
    }

    @Test
    void testCancelFail() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement psSelect = mock(PreparedStatement.class);
        PreparedStatement psDelete = mock(PreparedStatement.class);
        PreparedStatement psUpdate = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection).thenReturn(conn);

            // أي prepareStatement يرجع حسب الاستعلام
            when(conn.prepareStatement(contains("SELECT"))).thenReturn(psSelect);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(psDelete);
            when(conn.prepareStatement(contains("UPDATE"))).thenReturn(psUpdate);

            // SELECT يرجع بيانات صحيحة
            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("slot_id")).thenReturn(1);
            when(rs.getInt("participants")).thenReturn(2);

            // DELETE يفشل
            when(psDelete.executeUpdate()).thenReturn(0);

            boolean result = repo.cancel(10, 1);

            assertFalse(result);
        }
    }

    // ====================== update() ======================
    @Test
    void testUpdateSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(10, 1, 5);
            assertTrue(result);
        }
    }

    @Test
    void testUpdateFail() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(0);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<database_connection> mocked = mockStatic(database_connection.class)) {
            mocked.when(database_connection::getConnection).thenReturn(conn);

            boolean result = repo.update(10, 1, 5);
            assertFalse(result);
        }
    }
}