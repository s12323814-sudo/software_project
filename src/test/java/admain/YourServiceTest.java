package admain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;

class YourServiceTest {

    @Test
    void testSlotAvailable_withoutDB() throws Exception {
        // 1️⃣ Mock لكل شيء متعلق بالـ DB
        SlotRepository_y mockRepo = mock(SlotRepository_y.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        // 2️⃣ سلوك الـ mocks
        when(mockRepo.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(0); // slot متاح

        // 3️⃣ إنشاء الخدمة باستخدام الـ mock
        YourService service = new YourService(mockRepo);

        // 4️⃣ استدعاء الميثود
        boolean available = service.isSlotAvailableForResource(1, 1);

        // 5️⃣ تحقق
        assertTrue(available);
    }

    @Test
    void testSlotNotAvailable_withoutDB() throws Exception {
        SlotRepository_y mockRepo = mock(SlotRepository_y.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockRepo.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(5); // slot محجوز

        YourService service = new YourService(mockRepo);

        boolean available = service.isSlotAvailableForResource(1, 1);

        assertFalse(available);
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
}
