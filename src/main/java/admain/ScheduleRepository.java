package admain;

import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository {
    private List<Appointment> appointments = new ArrayList<>();

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public boolean isSlotAvailable(TimeSlot slot) {
        return appointments.stream()
            .noneMatch(a -> a.getTimeSlot().equals(slot)
                    && a.getStatus() == AppointmentStatus.CONFIRMED);
    }
}
