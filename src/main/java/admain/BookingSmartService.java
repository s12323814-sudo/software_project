package admain;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class BookingSmartService {

    private SlotRepository_y slotRepo;

    public BookingSmartService(SlotRepository_y slotRepo) {
        this.slotRepo = slotRepo;
    }

    
    public AppointmentSlot_y getNearestAvailableSlot(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();;

        return slots.stream()
                .filter(s -> s.getMaxCapacity() > 0)
                .min(Comparator.comparing(AppointmentSlot_y::getStartTime))
                .orElse(null);
    }

    public AppointmentSlot_y getBestSlot(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();
        return slots.stream()
                .filter(s -> s.getMaxCapacity() > 0)
                .max(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()))
                .orElse(null);
    }

    public List<AppointmentSlot_y> sortByTime(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();

        slots.sort(Comparator.comparing(AppointmentSlot_y::getStartTime));
        return slots;
    }

    public List<AppointmentSlot_y> sortByAvailability(Connection conn) throws SQLException {
        List<AppointmentSlot_y> slots = slotRepo.findAvailableSlots();
        slots.sort(Comparator.comparingInt(s -> s.getMaxCapacity() - s.getBookedCount()));
        return slots;
    }
}