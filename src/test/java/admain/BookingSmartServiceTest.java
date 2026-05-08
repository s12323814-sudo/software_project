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
    }@Test
void testGetFutureSlots_whenRepositoryReturnsNull_shouldReturnEmptyList() {
    when(slotRepoMock.findAvailableSlots()).thenReturn(null);

    List<AppointmentSlot_y> result = smartService.sortByAvailability();

    assertNotNull(result);
    assertTrue(result.isEmpty());
}@Test
void testGetFutureSlots_whenSlotHasNullDateOrTime_shouldBeFilteredOut() {
    AppointmentSlot_y badSlot = mock(AppointmentSlot_y.class);
    when(badSlot.getDate()).thenReturn(null);
    when(badSlot.getStartTime()).thenReturn(null);

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(badSlot));

    List<AppointmentSlot_y> result = smartService.sortByTime();

    assertTrue(result.isEmpty());
}@Test
void testGetNearestAvailableSlot_whenNoSlots_shouldReturnNull() {
    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

    AppointmentSlot_y result = smartService.getNearestAvailableSlot();

    assertNull(result);
}@Test
void testGetBestSlot_whenNoSlots_shouldReturnNull() {
    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

    AppointmentSlot_y result = smartService.getBestSlot();

    assertNull(result);
}@Test
void testGetBestSlot_whenAllSlotsZeroCapacity_shouldReturnNull() {
    AppointmentSlot_y slot = mock(AppointmentSlot_y.class);
    when(slot.getMaxCapacity()).thenReturn(0);
    when(slot.getBookedCount()).thenReturn(0);

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(slot));

    AppointmentSlot_y result = smartService.getBestSlot();

    assertNull(result);
}@Test
void testSortByTime_whenEmpty_shouldReturnEmptyList() {
    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

    List<AppointmentSlot_y> result = smartService.sortByTime();

    assertTrue(result.isEmpty());
}@Test
void testSortByAvailability_shouldSortCorrectly() {
    AppointmentSlot_y s1 = mock(AppointmentSlot_y.class);
    AppointmentSlot_y s2 = mock(AppointmentSlot_y.class);

    when(s1.getMaxCapacity()).thenReturn(10);
    when(s1.getBookedCount()).thenReturn(9); // 1

    when(s2.getMaxCapacity()).thenReturn(10);
    when(s2.getBookedCount()).thenReturn(2); // 8

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(s1, s2));

    List<AppointmentSlot_y> result = smartService.sortByAvailability();

    assertEquals(s1, result.get(0)); // الأقل availability أول
}
    @Test
void testGetFutureSlots_whenRepositoryThrowsException_shouldReturnEmptyList() {
    when(slotRepoMock.findAvailableSlots()).thenThrow(new RuntimeException("DB error"));

    List<AppointmentSlot_y> result = smartService.sortByTime();

    assertNotNull(result);
    assertTrue(result.isEmpty());
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
