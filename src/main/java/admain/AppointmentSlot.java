package admain;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSlot {

    private int slotId;
    private LocalDate slotDate;
    private LocalTime slotTime;
    private int capacity;
    private int bookedCount;

    public AppointmentSlot(int slotId, LocalDate slotDate, LocalTime slotTime, int capacity, int bookedCount) {
        this.slotId = slotId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
    }

    public int getSlotId() {
        return slotId;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    @Override
    public String toString() {
        return "Slot ID: " + slotId +
                " | Date: " + slotDate +
                " | Time: " + slotTime +
                " | Available Seats: " + (capacity - bookedCount);
    }
}