package admain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * يمثل هذا الكلاس Slot (فترة زمنية) يمكن حجزها للمواعيد.
 *
 * <p>يحتوي على معلومات مثل:</p>
 * <ul>
 *   <li>تاريخ الموعد</li>
 *   <li>وقت البداية والنهاية</li>
 *   <li>السعة القصوى</li>
 *   <li>عدد الحجوزات الحالية</li>
 * </ul>
 *
 * <p>يُستخدم هذا الكلاس لتحديد توفر المواعيد وإدارة السعة.</p>
 */
public class AppointmentSloty {

    private int id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxCapacity;
    private int bookedCount;

    /**
     * الحصول على اتصال بقاعدة البيانات.
     *
     * @return Connection
     * @throws SQLException في حال حدوث خطأ
     */
    protected Connection getConnection() throws SQLException {
        return database_connection.getConnection();
    }

    /**
     * Constructor لإنشاء Slot جديد.
     *
     * @param id رقم الـ Slot
     * @param date التاريخ
     * @param startTime وقت البداية
     * @param endTime وقت النهاية
     * @param maxCapacity السعة القصوى
     * @param bookedCount عدد المحجوزين حالياً
     */
    public AppointmentSlot_y(int id, LocalDate date, LocalTime startTime, LocalTime endTime, int maxCapacity, int bookedCount) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.bookedCount = bookedCount;
    }

    /**
     * تحويل وقت البداية إلى ZonedDateTime باستخدام المنطقة الزمنية Asia/Hebron.
     *
     * @return وقت البداية مع المنطقة الزمنية
     */
    public ZonedDateTime getStartDateTime() {
        return ZonedDateTime.of(date, startTime ,ZoneId.of("Asia/Hebron"));
    }

    /**
     * تحويل وقت النهاية إلى ZonedDateTime باستخدام المنطقة الزمنية Asia/Hebron.
     *
     * @return وقت النهاية مع المنطقة الزمنية
     */
    public ZonedDateTime getEndDateTime() {
        return ZonedDateTime.of(date, endTime,ZoneId.of("Asia/Hebron"));
    }

    /**
     * @return رقم الـ Slot
     */
    public int getId() {
        return id;
    }

    /**
     * @return تاريخ الـ Slot
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @return وقت البداية
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * @return وقت النهاية
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * @return السعة القصوى
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * @return عدد المحجوزين حالياً
     */
    public int getBookedCount() {
        return bookedCount;
    }

    /**
     * التحقق إذا كان الـ Slot ممتلئ.
     *
     * @return true إذا كانت السعة ممتلئة
     */
    public boolean isFull() {
        return bookedCount >= maxCapacity;
    }

    /**
     * التحقق إذا كان الـ Slot متاح لمورد معين (Resource).
     *
     * <p>يتم التحقق عبر قاعدة البيانات إذا كان هناك حجز لنفس المورد في هذا الـ Slot.</p>
     *
     * @param slotId رقم الـ Slot
     * @param resourceId رقم المورد
     * @return true إذا كان متاح، false إذا غير متاح
     */
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

    /**
     * إرجاع تمثيل نصي للـ Slot.
     *
     * @return نص يحتوي على تفاصيل الـ Slot
     */
    @Override
    public String toString() {
        return "ID: " + id +
                " | Date: " + date +
                " | Start: " + startTime +
                " | End: " + endTime +
                " | Capacity: " + bookedCount + "/" + maxCapacity;
    }
}
