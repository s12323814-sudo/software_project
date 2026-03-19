package admain;

import java.time.LocalDate;


import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class AppointmentSlot_y {

    private int id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxCapacity;
    private int bookedCount;


    public AppointmentSlot_y(int id, LocalDate date, LocalTime startTime, LocalTime endTime, int maxCapacity, int bookedCount) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.bookedCount = bookedCount;
      
        
        
    }
   
    public ZonedDateTime getStartDateTime() {
        return ZonedDateTime.of(date, startTime ,ZoneId.of("Asia/Hebron"));
    }

    public ZonedDateTime getEndDateTime() {
        return ZonedDateTime.of(date, endTime,ZoneId.of("Asia/Hebron"));
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
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

 
    @Override
    public String toString() {
        return "ID: " + id +
                " | Date: " + date +
                " | Start: " + startTime +
                " | End: " + endTime +
                " | Capacity: " + bookedCount + "/" + maxCapacity;
    }
}