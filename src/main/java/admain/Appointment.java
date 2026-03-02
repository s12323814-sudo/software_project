package admain;

public class Appointment {
    private TimeSlot timeSlot;
    private int participants;
    private AppointmentStatus status;
    private String type;

    public Appointment(TimeSlot timeSlot, int participants, String type) {
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.type = type;
        this.status = AppointmentStatus.CONFIRMED;
        this.timeSlot.setAvailable(false);
    }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getParticipants() { return participants; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getType() { return type; }
}