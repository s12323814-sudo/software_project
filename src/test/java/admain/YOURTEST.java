package admain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YOURTEST {

    private SlotRepository_y repo;
    private YourService service;

    @BeforeEach
    public void setUp() {
        repo = mock(SlotRepository_y.class);
        service = new YourService(repo);
    }

    @Test
    public void testSlotAvailable_returnsTrue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(0); // count = 0 → متاح

        boolean result = service.isSlotAvailableForResource(1, 10);
        assertTrue(result);
    }

    @Test
    public void testSlotAvailable_returnsFalse() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(3); // count > 0 → مش متاح

        boolean result = service.isSlotAvailableForResource(1, 10);
        assertFalse(result);
    }

    @Test
    public void testSlotAvailable_SQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(repo.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        assertThrows(RuntimeException.class, () -> 
            service.isSlotAvailableForResource(1, 10)
        );
    }
}