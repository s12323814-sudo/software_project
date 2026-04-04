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

        // مواعيد مستقبلية دائمًا لتجنب الفلترة
        slot1 = new AppointmentSlot_y(1,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                10, 5);

        slot2 = new AppointmentSlot_y(2,
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                8, 2);

        slot3 = new AppointmentSlot_y(3,
                LocalDate.now().plusDays(3),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                5, 5);

        // DB mock
        when(slotRepoMock.findAvailableSlots()).thenReturn(Arrays.asList(slot1, slot2, slot3));
    }
    @Test
    void testGetNearestAvailableSlot() {
        AppointmentSlot_y nearest = smartService.getNearestAvailableSlot();
        assertNotNull(nearest);
        assertEquals(slot1.getId(), nearest.getId()); // أقرب موعد مستقبلي
    }

    @Test
    void testSortByTime() {
        List<AppointmentSlot_y> sorted = smartService.sortByTime();
        assertEquals(3, sorted.size());
        assertEquals(slot1.getId(), sorted.get(0).getId());
        assertEquals(slot2.getId(), sorted.get(1).getId());
        assertEquals(slot3.getId(), sorted.get(2).getId());
    }

    @Test
    void testSortByAvailability() {
        List<AppointmentSlot_y> sorted = smartService.sortByAvailability();
        assertEquals(3, sorted.size());

        // أقل available أول: 0, 5, 6
        assertEquals(slot3.getId(), sorted.get(0).getId()); // 5-5=0
        assertEquals(slot1.getId(), sorted.get(1).getId()); // 10-5=5
        assertEquals(slot2.getId(), sorted.get(2).getId()); // 8-2=6
    }
}