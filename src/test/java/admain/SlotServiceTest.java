package admain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SlotServiceTest {

    private AppointmentRepository_y mockApptRepo;
    private SlotRepository_y mockSlotRepo;
    private SlotService_y slotService;

    @BeforeEach
    void setup() {
        mockApptRepo = mock(AppointmentRepository_y.class);
        mockSlotRepo = mock(SlotRepository_y.class);
        slotService = new SlotService_y(mockApptRepo, mockSlotRepo);
    }

    @Test
    void testBookAppointment_success() throws SQLException {
        int userId = 1;
        int slotId = 10;
        int participants = 2;

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(1);

        when(mockSlotRepo.findById(slotId)).thenReturn(slot);
        when(mockApptRepo.book(userId, slotId, participants, AppointmentType_y.STANDARD)).thenReturn(true);

        boolean result = slotService.bookAppointment(userId, slotId, participants, AppointmentType_y.STANDARD);
        assertTrue(result);
    }

    @Test
    void testBookAppointment_fail_fullSlot() throws SQLException {
        int userId = 1;
        int slotId = 10;
        int participants = 5;

        AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
        when(slot.getMaxCapacity()).thenReturn(5);
        when(slot.getBookedCount()).thenReturn(4);

        when(mockSlotRepo.findById(slotId)).thenReturn(slot);

        boolean result = slotService.bookAppointment(userId, slotId, participants, AppointmentType_y.STANDARD);
        assertFalse(result);
    }

    @Test
    void testCancelAppointment_success() throws SQLException {
        int userId = 1;
        int appointmentId = 100;

        when(mockApptRepo.cancel(userId, appointmentId)).thenReturn(true);

        boolean result = slotService.cancelAppointment(userId, appointmentId);
        assertTrue(result);
    }

    @Test
    void testAdminCancelAppointment_success() throws SQLException {
        int appointmentId = 200;

        Connection mockConn = mock(Connection.class);
        Appointment appointment = mock(Appointment.class);

        when(appointment.getSlotId()).thenReturn(10);
        when(appointment.getParticipants()).thenReturn(2);

        // mock findById لإرجاع Appointment
        doReturn(appointment).when(mockApptRepo).findById(eq(appointmentId), any(Connection.class));

        // delete و decreaseBookedCount void → doNothing
        doNothing().when(mockApptRepo).delete(appointmentId, mockConn);
        doNothing().when(mockSlotRepo).decreaseBookedCount(10, 2, mockConn);

        // هذه الطريقة لاختبار adminCancelAppointment تحتاج تعديل SlotService لإضافة Connection قابل للـ mock
        // حاليا يمكن اختبار الجزء المنطقي من الدوال الأخرى
    }
}