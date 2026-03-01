package admain;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        AppointmentService service = new AppointmentService();

        TimeSlot slot1 = new TimeSlot(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 1, 11, 0)
        );

        Appointment appt1 = new Appointment(slot1, 3, "In-Person");
        service.bookAppointment(appt1);

        Appointment appt2 = new Appointment(slot1, 2, "Virtual");
        service.bookAppointment(appt2);

        service.getAppointments().forEach(a ->
            System.out.println(a.getTimeSlot().getStart() + " - " +
                               a.getTimeSlot().getEnd() + " | Participants: " +
                               a.getParticipants() + " | Type: " + a.getType())
        );
    }
}