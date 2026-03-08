package admain;

import java.time.Duration;

public class Appointment {

    private TimeSlot timeSlot;
    private int participants;
    private AppointmentStatus status;
    private String type;

    public Appointment(TimeSlot timeSlot, int participants, String type) {
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.type = type;
        this.status = AppointmentStatus.PENDING; // يبدأ Pending
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getParticipants() {
        return participants;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

   
    public int getDuration() {
        return (int) Duration.between(timeSlot.getStart(), timeSlot.getEnd()).toMinutes();
    }

   
    @Override
    public String toString() {
        return "Appointment{" +
                "Start=" + timeSlot.getStart() +
                ", End=" + timeSlot.getEnd() +
                ", Participants=" + participants +
                ", Status=" + status +
                ", Type='" + type + '\'' +
                '}';
    }
}