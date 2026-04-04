package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    protected Connection getConnection() throws SQLException {
        return database_connection.getConnection();
    }
    
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

    public boolean isSlotAvailableForResource(int slotId, int resourceId) {
        String sql = "SELECT COUNT(*) FROM appointment_slot a " +
                     "JOIN appointment b ON a.slot_id = b.slot_id " +
                     "WHERE a.slot_id = ? AND b.resource_id = ?";
        SlotRepository_y repo = new SlotRepository_y();
        try (Connection conn = repo.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.setInt(2, resourceId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0; 
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
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