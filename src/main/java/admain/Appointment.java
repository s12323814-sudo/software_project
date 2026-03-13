package admain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private TimeSlot timeSlot;
    private int participants;
    private String status;
    private String type;
    private int bookedCount;
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
    private List<Booking_y> bookings = new ArrayList<>();

    public List<Booking_y> getBookings() {
        return bookings;
    }

    public void addBooking(Booking_y booking) {
        bookings.add(booking);
        bookedCount++;
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