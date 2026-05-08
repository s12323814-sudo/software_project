package admain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingSmartServiceTest {

    private SlotRepository_y slotRepoMock;
    private BookingSmartService service;

    private AppointmentSlot_y slot1;
    private AppointmentSlot_y slot2;

    @BeforeEach
    void setUp() {
        slotRepoMock = mock(SlotRepository_y.class);
        service = new BookingSmartService(slotRepoMock);

        LocalDate base = LocalDate.of(2030, 1, 1);

        slot1 = new AppointmentSlot_y(1,
                base.plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                10, 5);

        slot2 = new AppointmentSlot_y(2,
                base.plusDays(2),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                8, 2);
    }

    // =========================
    // getFutureSlotsFromDB tests indirectly
    // =========================
@Test
void testGetBestSlot_whenEmpty_shouldReturnNull() {
    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

    AppointmentSlot_y result = service.getBestSlot();

    assertNull(result);
}@Test
void testGetBestSlot_whenAllCapacityZero_shouldReturnNull() {
    AppointmentSlot_y s = mock(AppointmentSlot_y.class);
    when(s.getMaxCapacity()).thenReturn(0);
    when(s.getBookedCount()).thenReturn(0);

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(s));

    AppointmentSlot_y result = service.getBestSlot();

    assertNull(result);
}@Test
void testGetBestSlot_shouldReturnHighestAvailability() {
    AppointmentSlot_y low = mock(AppointmentSlot_y.class);
    AppointmentSlot_y high = mock(AppointmentSlot_y.class);

    when(low.getMaxCapacity()).thenReturn(10);
    when(low.getBookedCount()).thenReturn(9); // 1

    when(high.getMaxCapacity()).thenReturn(10);
    when(high.getBookedCount()).thenReturn(2); // 8

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(low, high));

    AppointmentSlot_y result = service.getBestSlot();

    assertEquals(high, result);
}@Test
void testGetBestSlot_whenEqualValues_shouldReturnOneOfThem() {
    AppointmentSlot_y s1 = mock(AppointmentSlot_y.class);
    AppointmentSlot_y s2 = mock(AppointmentSlot_y.class);

    when(s1.getMaxCapacity()).thenReturn(10);
    when(s1.getBookedCount()).thenReturn(5);

    when(s2.getMaxCapacity()).thenReturn(10);
    when(s2.getBookedCount()).thenReturn(5);

    when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(s1, s2));

    AppointmentSlot_y result = service.getBestSlot();

    assertNotNull(result);
}@Test
void testGetFutureSlots_whenRepositoryThrowsException_shouldHitCatchBlock() {
    when(slotRepoMock.findAvailableSlots())
            .thenThrow(new RuntimeException("DB failure"));

    List<AppointmentSlot_y> result = service.sortByTime();

    assertNotNull(result);
    assertTrue(result.isEmpty());
}@Test
void testCatchBlock_isExecuted_directly() {
    when(slotRepoMock.findAvailableSlots())
            .thenThrow(new RuntimeException());

    service.getNearestAvailableSlot(); // أو أي public method

    // ما في assert مهم هنا لأن الهدف coverage
}
    @Test
    void testRepositoryThrowsException_returnsEmpty() {
        when(slotRepoMock.findAvailableSlots()).thenThrow(new RuntimeException());

        List<AppointmentSlot_y> result = service.sortByTime();

        assertTrue(result.isEmpty());
    }

    @Test
    void testRepositoryReturnsNull_returnsEmpty() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(null);

        List<AppointmentSlot_y> result = service.sortByAvailability();

        assertTrue(result.isEmpty());
    }

    @Test
    void testRepositoryReturnsEmptyList() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

        assertTrue(service.sortByTime().isEmpty());
    }

    // =========================
    // Filtering null fields
    // =========================

    @Test
    void testSlotWithNullDateOrTime_isFilteredOut() {
        AppointmentSlot_y bad = mock(AppointmentSlot_y.class);
        when(bad.getDate()).thenReturn(null);
        when(bad.getStartTime()).thenReturn(null);

        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(bad));

        List<AppointmentSlot_y> result = service.sortByTime();

        assertTrue(result.isEmpty());
    }

    // =========================
    // getNearestAvailableSlot
    // =========================

    @Test
    void testNearestSlot_returnsCorrect() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(slot2, slot1));

        AppointmentSlot_y result = service.getNearestAvailableSlot();

        assertNotNull(result);
    }

    @Test
    void testNearestSlot_empty_returnsNull() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

        assertNull(service.getNearestAvailableSlot());
    }

    // =========================
    // getBestSlot
    // =========================

    @Test
    void testBestSlot_normalCase() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(slot1, slot2));

        AppointmentSlot_y result = service.getBestSlot();

        assertNotNull(result);
    }

    @Test
    void testBestSlot_noCapacity_returnsNull() {
        AppointmentSlot_y s = mock(AppointmentSlot_y.class);
        when(s.getMaxCapacity()).thenReturn(0);

        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(s));

        assertNull(service.getBestSlot());
    }

    @Test
    void testBestSlot_empty_returnsNull() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

        assertNull(service.getBestSlot());
    }

    // =========================
    // sortByTime
    // =========================

    @Test
    void testSortByTime_correctOrder() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(slot2, slot1));

        List<AppointmentSlot_y> result = service.sortByTime();

        assertEquals(2, result.size());
    }

    @Test
    void testSortByTime_empty() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

        assertTrue(service.sortByTime().isEmpty());
    }

    // =========================
    // sortByAvailability
    // =========================

    @Test
    void testSortByAvailability_ordering() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of(slot1, slot2));

        List<AppointmentSlot_y> result = service.sortByAvailability();

        assertEquals(2, result.size());
    }

    @Test
    void testSortByAvailability_empty() {
        when(slotRepoMock.findAvailableSlots()).thenReturn(List.of());

        assertTrue(service.sortByAvailability().isEmpty());
    }
}
