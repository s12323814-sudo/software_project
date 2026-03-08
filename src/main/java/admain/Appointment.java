package admain;

import java.time.Duration;

public class Appointment {
    private TimeSlot timeSlot;
    private int participants;
    private String status;
    private String type;

    public Appointment(TimeSlot timeSlot, int participants, String type) {
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.type = type;
        this.status = "PENDING";
    }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getParticipants() { return participants; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }

    public int getDuration() {
        return (int) Duration.between(timeSlot.getStart(), timeSlot.getEnd()).toMinutes();
    }

    @Override
    public String toString() {
        return "Appointment{Start=" + timeSlot.getStart() +
                ", End=" + timeSlot.getEnd() +
                ", Participants=" + participants +
                ", Status=" + status +
                ", Type='" + type + "'}";
    }
}