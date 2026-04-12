package admain;

/**
 * يمثل هذا الكلاس موعد (Appointment) داخل النظام.
 *
 * <p>يحتوي على معلومات الحجز مثل المستخدم، الوقت (TimeSlot)،
 * عدد المشاركين، حالة الموعد، ونوعه.</p>
 *
 * <p>يمكن استخدام هذا الكلاس مع قاعدة البيانات حيث يحتوي على slotId
 * بالإضافة إلى كائن TimeSlot في حال تم عمل JOIN.</p>
 */
public class Appointment {

    private int appointmentId;     // Primary Key
    private int userId;
    private int slotId;
    private TimeSlot timeSlot;
    private int participants;
    private AppointmentStatus_y status;
    private AppointmentType_y type;
    private String username;

    /**
     * @return اسم المستخدم المرتبط بالموعد
     */
    public String getUsername() {
        return username;
    }

    /**
     * تعيين اسم المستخدم.
     *
     * @param username اسم المستخدم
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Constructor فارغ (مطلوب في بعض الحالات مثل ORM أو JDBC).
     */
    public Appointment() {
    }

    /**
     * إنشاء موعد مع جميع التفاصيل بما في ذلك TimeSlot.
     *
     * @param appointmentId رقم الموعد
     * @param userId رقم المستخدم
     * @param slotId رقم الوقت (TimeSlot)
     * @param timeSlot كائن الوقت (اختياري)
     * @param participants عدد المشاركين
     * @param status حالة الموعد
     * @param type نوع الموعد
     */
    public Appointment(int appointmentId, int userId, int slotId,
                       TimeSlot timeSlot, int participants,
                       AppointmentStatus_y status,
                       AppointmentType_y type) {

        this.appointmentId = appointmentId;
        this.userId = userId;
        this.slotId = slotId;
        this.timeSlot = timeSlot;
        this.participants = participants;
        this.status = status;
        this.type = type;
    }

    /**
     * إنشاء موعد بدون كائن TimeSlot (يُستخدم عند عدم وجود JOIN).
     *
     * @param appointmentId رقم الموعد
     * @param userId رقم المستخدم
     * @param slotId رقم الوقت
     * @param participants عدد المشاركين
     * @param status حالة الموعد
     * @param type نوع الموعد
     */
    public Appointment(int appointmentId, int userId, int slotId,
                       int participants,
                       AppointmentStatus_y status,
                       AppointmentType_y type) {

        this.appointmentId = appointmentId;
        this.userId = userId;
        this.slotId = slotId;
        this.participants = participants;
        this.status = status;
        this.type = type;
    }

    /**
     * @return رقم الموعد
     */
    public int getAppointmentId() {
        return appointmentId;
    }

    /**
     * @return رقم المستخدم
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return رقم الـ TimeSlot
     */
    public int getSlotId() {
        return slotId;
    }

    /**
     * @return كائن الوقت (TimeSlot) إن وجد
     */
    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    /**
     * @return عدد المشاركين
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * @return حالة الموعد
     */
    public AppointmentStatus_y getStatus() {
        return status;
    }

    /**
     * @return نوع الموعد
     */
    public AppointmentType_y getType() {
        return type;
    }

    /**
     * تعيين حالة الموعد.
     *
     * @param status الحالة الجديدة
     */
    public void setStatus(AppointmentStatus_y status) {
        this.status = status;
    }

    /**
     * تعيين عدد المشاركين.
     *
     * @param participants العدد الجديد
     */
    public void setParticipants(int participants) {
        this.participants = participants;
    }

    /**
     * تعيين كائن الوقت.
     *
     * @param timeSlot كائن TimeSlot
     */
    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    /**
     * إرجاع تمثيل نصي للموعد.
     *
     * @return نص يحتوي على تفاصيل الموعد
     */
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + appointmentId +
                ", userId=" + userId +
                ", slotId=" + slotId +
                ", participants=" + participants +
                ", status=" + status +
                ", type=" + type +
                (timeSlot != null ? ", timeSlot=" + timeSlot : "") +
                '}';
    }
}