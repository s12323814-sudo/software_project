package admain;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentRepoQueryTes {

    @Test
    void testGetAllAppointments_success() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<database_connection> mocked =
                     mockStatic(database_connection.class)) {

            mocked.when(database_connection::getConnection)
                    .thenReturn(conn);

            when(conn.prepareStatement(anyString()))
                    .thenReturn(stmt);

            when(stmt.executeQuery()).thenReturn(rs);

            // 1 row only
            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("appointment_id")).thenReturn(1);
            when(rs.getInt("account_id")).thenReturn(10);
            when(rs.getInt("slot_id")).thenReturn(5);
            when(rs.getInt("participants")).thenReturn(3);

            when(rs.getString("status")).thenReturn("CONFIRMED");
            when(rs.getString("type")).thenReturn("ONLINE");
            when(rs.getString("username")).thenReturn("lana");

            AppointmentRepository_y repo = new AppointmentRepository_y();

            List<Appointment> list = repo.getAllAppointments(1);

            // ✅ assertions
            assertEquals(1, list.size());

            Appointment a = list.get(0);

            assertEquals(1, a.getAppointmentId());
            assertEquals(10, a.getUserId());
            assertEquals(5, a.getSlotId());
            assertEquals(3, a.getParticipants());
            assertEquals(AppointmentStatus_y.CONFIRMED, a.getStatus());
            assertEquals(AppointmentType_y.ONLINE, a.getType());
            assertEquals("lana", a.getUsername());

            // verify flow
            verify(stmt).setInt(1, 1);
            verify(stmt).executeQuery();
        }
    }
}