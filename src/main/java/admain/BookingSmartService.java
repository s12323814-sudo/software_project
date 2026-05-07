package admain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class BookingSmartService {

    private SlotRepository_y slotRepo;

    public BookingSmartService(SlotRepository_y slotRepo) {
        this.slotRepo = slotRepo;
    }

    private List<AppointmentSlot_y> getFutureSlotsFromDB() {
        List<AppointmentSlot_y> slots;
        try {
            slots = slotRepo.findAvailableSlots();
        } catch (Exception e) {
          
            return new ArrayList<>();
        }

        if (slots == null) return new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        return slots.stream()
                .filter(s -> s.getDate() != null && s.getStartTime() != null)
                .filter(s -> !LocalDateTime.of(s.getDate(), s.getStartTime()).isBefore(now))
                .toList();
    }

    public AppointmentSlot_y getNearestAvailableSlot() {
        List<AppointmentSlot_y> slots = getFutureSlotsFromDB();

        return slots.stream()
                .min(Comparator.comparing(s -> LocalDateTime.of(s.getDate(), s.getStartTime())))
                .orElse(null); // ما في شيء → ترجع null
    }


    public AppointmentSlot_y getBestSlot() {
        List<AppointmentSlot_y> slots = getFutureSlotsFromDB();

        return slots.stream()
                .filter(s -> s.getMaxCapacity() > 0)
                .max(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()))
                .orElse(null);
    }


    public List<AppointmentSlot_y> sortByTime() {

        List<AppointmentSlot_y> slots =
                new ArrayList<>(getFutureSlotsFromDB()); // 👈 الحل

        slots.sort(Comparator.comparing(AppointmentSlot_y::getDate)
                .thenComparing(AppointmentSlot_y::getStartTime));

        return slots;
    }

    public List<AppointmentSlot_y> sortByAvailability() {
        List<AppointmentSlot_y> slots = getFutureSlotsFromDB();

        return slots.stream()
                .sorted(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()))
                .toList();
    }
}
