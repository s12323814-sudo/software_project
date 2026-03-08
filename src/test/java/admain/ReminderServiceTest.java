package admain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReminderServiceTest {

    @Test
    public void testReminderOutput() {

        
        AppointmentSlot slot = new AppointmentSlot(
                1,                       // slotId
                LocalDate.now(),         // slotDate
                LocalTime.now().plusMinutes(30), // slotTime بعد 30 دقيقة
                10,                      // maxCapacity
                0                        // bookedCount
        );
        slot.addBooking(new Booking("user@example.com")); // booking وهمي

       
        SlotService mockSlotService = new SlotService() {
            @Override
            public List<AppointmentSlot> getAvailableSlots() {
                return List.of(slot);
            }
        };

        
        MockNotificationService mockNotification = new MockNotificationService();

        
        ReminderManager  reminderService = new ReminderManager(mockSlotService, mockNotification);

       
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        reminderService.checkReminders();

        System.setOut(originalOut);

        
        String result = output.toString();
        System.out.println("Captured output:\n" + result); 
        assertTrue(result.contains("Reminder"));
        assertTrue(result.contains("user@example.com"));

        
        List<String> sent = mockNotification.getSentMessages();
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains("Reminder"));
        assertTrue(sent.get(0).contains("user@example.com"));
    }
}