package admain;

public class Appointment {

    private int userId;
    private TimeSlot timeSlot;
    private int participants;
    private String status;

    // Constructor كامل
    public Appointment(int userId, TimeSlot timeSlot, int participants, String status) {
        this.userId = userId;
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.status = status;
    }

    // Constructor بدون userId (اختياري)
    public Appointment(TimeSlot timeSlot, int participants, String status) {
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.status = status;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getParticipants() {
        return participants;
    }

    public String getStatus() {
        return status;
    }

    // Setter للـ status
    public void setStatus(String status) {
        this.status = status;
    }
}
