package admain;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookingSmartService {

    private SlotRepository_y slotRepo;

    public BookingSmartService(SlotRepository_y slotRepo) {
        this.slotRepo = slotRepo;
    }

    public AppointmentSlot_y getNearestAvailableSlot(List<AppointmentSlot_y> slots) {
        if (slots == null || slots.isEmpty()) return null;

        LocalDateTime now = LocalDateTime.now();

        return slots.stream()
                .filter(s -> s.getDate() != null && s.getStartTime() != null)
                .filter(s -> LocalDateTime.of(s.getDate(), s.getStartTime()).isAfter(now))
                .min(Comparator.comparing(s -> LocalDateTime.of(s.getDate(), s.getStartTime())))
                .orElse(null);
    }

    public AppointmentSlot_y getBestSlot(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();
        if (slots == null || slots.isEmpty()) return null;

        return slots.stream()
                .filter(s -> s.getMaxCapacity() > 0)
                .max(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()))
                .orElse(null);
    }
    public List<AppointmentSlot_y> sortByTime(List<AppointmentSlot_y> slots) {
        if (slots == null || slots.isEmpty()) {
            slots = slotRepo.findAvailableSlots();
            if (slots == null) return new ArrayList<>();
        }
       slots.sort(Comparator.comparing(AppointmentSlot_y::getDate)
                             .thenComparing(AppointmentSlot_y::getStartTime));
        return slots;
    }

    public List<AppointmentSlot_y> sortByAvailability(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();
        if (slots == null) return new ArrayList<>();

        slots.sort(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()));
        return slots;
    }
}