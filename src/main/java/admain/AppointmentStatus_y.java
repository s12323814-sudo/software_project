package admain;

/**
 * يمثل هذا الـ enum حالات وأنواع مرتبطة بالموعد (Appointment).
 *
 * <p>يُستخدم لتحديد حالة الموعد أو تصنيفه داخل النظام.</p>
 *
 * <p>القيم المتاحة:</p>
 * <ul>
 *   <li>COMPLETED - الموعد مكتمل</li>
 *   <li>STANDARD - موعد عادي</li>
 *   <li>PREMIUM - موعد مميز</li>
 *   <li>VIP - موعد VIP</li>
 *   <li>ONGOING - الموعد جارٍ حالياً</li>
 *   <li>CONFIRMED - الموعد مؤكد</li>
 *   <li>WAITLIST - على قائمة الانتظار</li>
 *   <li>PENDING - قيد الانتظار</li>
 *   <li>CANCELLED - تم إلغاء الموعد</li>
 * </ul>
 *
 
 */
public enum AppointmentStatus_y {
    COMPLETED,
    STANDARD,
    PREMIUM,
    VIP,
    ONGOING,
    CONFIRMED,
    WAITLIST,
    PENDING,
    CANCELLED
}
