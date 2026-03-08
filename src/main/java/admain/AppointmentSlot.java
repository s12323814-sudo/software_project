package admain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentSlot {

    private int id;
    private LocalDate date;
    private LocalTime time;
    private int maxCapacity;
    private int bookedCount;

    public AppointmentSlot(int id, LocalDate date, LocalTime time, int maxCapacity, int bookedCount) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.maxCapacity = maxCapacity;
        this.bookedCount = bookedCount;
    }
    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time);
    }
    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getBookedCount() {
        return bookedCount;
    }

  
    public boolean isFull() {
        return bookedCount >= maxCapacity;
    }

    private List<Booking> bookings = new ArrayList<>();

    public List<Booking> getBookings() {
        return bookings;
    }
    public void addBooking(Booking booking) {
        bookings.add(booking);
        bookedCount++;
    }

    @Override
    public String toString() {
        return "ID: " + id +
                " | Date: " + date +
                " | Time: " + time +
                " | Capacity: " + bookedCount + "/" + maxCapacity;
    }
}