package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingSmartServiceTest {

    private SlotRepository_y slotRepoMock;
    private BookingSmartService smartService;

    private AppointmentSlot_y slot1;
    private AppointmentSlot_y slot2;
    private AppointmentSlot_y slot3;

    @BeforeEach
    void setUp() {
        slotRepoMock = mock(SlotRepository_y.class);
        smartService = new BookingSmartService(slotRepoMock);

      
        slot1 = new AppointmentSlot_y(1,
                LocalDate.of(2026, 4, 5),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                10, 5);

        slot2 = new AppointmentSlot_y(2,
                LocalDate.of(2026, 4, 5),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                8, 2);

        slot3 = new AppointmentSlot_y(3,
                LocalDate.of(2026, 4, 6),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                5, 5);
    }

    @Test
    void testGetNearestAvailableSlot() {
        List<AppointmentSlot_y> slots = Arrays.asList(slot1, slot2, slot3);
        AppointmentSlot_y nearest = smartService.getNearestAvailableSlot(slots);

        assertNotNull(nearest);
        assertEquals(slot1.getId(), nearest.getId()); // أقرب وقت متاح
    }

    @Test
    void testGetBestSlot() throws Exception {
        when(slotRepoMock.findAvailableSlots()).thenReturn(Arrays.asList(slot1, slot2, slot3));

        AppointmentSlot_y best = smartService.getBestSlot(null);

        assertNotNull(best);
     
        assertEquals(slot2.getId(), best.getId());
    }

    @Test
    void testSortByTime() {
        List<AppointmentSlot_y> slots = Arrays.asList(slot2, slot3, slot1);
        List<AppointmentSlot_y> sorted = smartService.sortByTime(slots);

        assertEquals(3, sorted.size());
        assertEquals(slot1.getId(), sorted.get(0).getId());
        assertEquals(slot2.getId(), sorted.get(1).getId());
        assertEquals(slot3.getId(), sorted.get(2).getId());
    }

    @Test
    void testSortByAvailability() throws Exception {
        when(slotRepoMock.findAvailableSlots()).thenReturn(Arrays.asList(slot1, slot2, slot3));

        List<AppointmentSlot_y> sorted = smartService.sortByAvailability(null);

        assertEquals(3, sorted.size());
       
        assertEquals(slot3.getId(), sorted.get(0).getId());
        assertEquals(slot1.getId(), sorted.get(1).getId());
        assertEquals(slot2.getId(), sorted.get(2).getId());
    }
}