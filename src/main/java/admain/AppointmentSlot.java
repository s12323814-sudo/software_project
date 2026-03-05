package admain;

import java.time.LocalDate;
import java.time.LocalTime;

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

    // تحقق إذا كان الـ slot ممتلئ
    public boolean isFull() {
        return bookedCount >= maxCapacity;
    }

    // زيادة عدد الحجوزات
    public void addBooking() {
        if (!isFull()) {
            bookedCount++;
        }
    }

    @Override
    public String toString() {
        return "ID: " + id +
                " | Date: " + date +
                " | Time: " + time +
                " | Capacity: " + bookedCount + "/" + maxCapacity;
    }
}