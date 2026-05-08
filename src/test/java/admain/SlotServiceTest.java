package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingSmartServiceTest {

    private SlotRepository_y slotRepo;
    private BookingSmartService service;

    @BeforeEach
    void setUp() {
        slotRepo = mock(SlotRepository_y.class);
        service = new BookingSmartService(slotRepo);
    }

    // ================= BEST SLOT (HIGHEST AVAILABILITY) =================

    @Test
    void testGetBestSlot_shouldReturnHighestAvailability() {

        AppointmentSlot_y slot1 = mock(AppointmentSlot_y.class);
        AppointmentSlot_y slot2 = mock(AppointmentSlot_y.class);

        when(slot1.getMaxCapacity()).thenReturn(10);
        when(slot1.getBookedCount()).thenReturn(2); // 8 available

        when(slot2.getMaxCapacity()).thenReturn(10);
        when(slot2.getBookedCount()).thenReturn(5); // 5 available

        when(slotRepo.findAvailableSlots())
                .thenReturn(Arrays.asList(slot1, slot2));

        AppointmentSlot_y result = service.getBestSlot();

        assertNotNull(result);
        assertEquals(slot1, result);
    }

    // ================= EQUAL VALUES =================

    @Test
    void testGetBestSlot_whenEqualValues_shouldReturnOneOfThem() {

        AppointmentSlot_y slot1 = mock(AppointmentSlot_y.class);
        AppointmentSlot_y slot2 = mock(AppointmentSlot_y.class);

        when(slot1.getMaxCapacity()).thenReturn(10);
        when(slot1.getBookedCount()).thenReturn(5);

        when(slot2.getMaxCapacity()).thenReturn(10);
        when(slot2.getBookedCount()).thenReturn(5);

        when(slotRepo.findAvailableSlots())
                .thenReturn(Arrays.asList(slot1, slot2));

        AppointmentSlot_y result = service.getBestSlot();

        assertNotNull(result);
        assertTrue(result == slot1 || result == slot2);
    }

    // ================= EMPTY LIST =================

    @Test
    void testGetBestSlot_whenEmpty_shouldReturnNull() {

        when(slotRepo.findAvailableSlots())
                .thenReturn(Arrays.asList());

        AppointmentSlot_y result = service.getBestSlot();

        assertNull(result);
    }

    // ================= NULL LIST SAFETY =================

    @Test
    void testGetBestSlot_whenNull_shouldReturnNull() {

        when(slotRepo.findAvailableSlots())
                .thenReturn(null);

        AppointmentSlot_y result = service.getBestSlot();

        assertNull(result);
    }
}
