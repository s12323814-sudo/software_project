package admain;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class SlotService_y {

    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
   // private static Scanner sc = new Scanner(System.in);
    // Dependency Injection
    public SlotService_y(AppointmentRepository_y appointmentRepo,
                         SlotRepository_y slotRepo) {
        this.appointmentRepo = appointmentRepo;
        this.slotRepo = slotRepo;
    }

    /////////////////////////////
    // GET AVAILABLE SLOTS
    public List<AppointmentSlot_y> getAvailableSlots() {
        return slotRepo.findAvailableSlots();
    }

    /////////////////////////////
    // BOOK APPOINTMENT
    public boolean bookAppointment(int userId, int slotId, int participants,AppointmentType_y type) throws SQLException {

        if (participants <= 0) return false;

        AppointmentSlot_y slot = slotRepo.findById(slotId);

        if (slot == null) return false;

        int remaining = slot.getMaxCapacity() - slot.getBookedCount();

        if (participants > remaining) return false;

        return appointmentRepo.book(userId, slotId, participants,type);
    }

    /////////////////////////////
    // CANCEL (USER)
    public boolean cancelAppointment(int userId, int appointmentId) throws SQLException {
        return appointmentRepo.cancel(userId, appointmentId);
    }

    /////////////////////////////
    // UPDATE
    public boolean updateAppointment(int userId, int appointmentId, int participants) throws SQLException {
        return appointmentRepo.update(userId, appointmentId, participants);
    }

    /////////////////////////////
    // VIEW USER APPOINTMENTS
    public List<Appointment> viewUserAppointments(int userId) throws SQLException {
        return appointmentRepo.getUserUpcomingAppointments(userId);
    }

    /////////////////////////////
    // ADMIN: ADD SLOT
    public boolean addSlot(LocalDate date, LocalTime start, LocalTime end,
                           int capacity, int adminId) {

        if (capacity <= 0) return false;

        return slotRepo.addSlot(date, start, end, capacity, adminId);
    }

    /////////////////////////////
    // ADMIN: CANCEL WITH TRANSACTION (IMPORTANT)
    public boolean adminCancelAppointment(int appointmentId) throws SQLException {

        try (Connection conn = database_connection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Appointment appointment = appointmentRepo.findById(appointmentId, conn);

                if (appointment == null) {
                    conn.rollback();
                    return false;
                }

                appointmentRepo.delete(appointmentId, conn);

                slotRepo.decreaseBookedCount(
                        appointment.getSlotId(),
                        appointment.getParticipants(),
                        conn
                );

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        }}
    public void addSlot(Account_y admin, LocalDate date, LocalTime start,
            LocalTime end, int capacity) {

if (admin == null || !admin.isAdmin()) {
System.out.println("Admin only!");
return;
}

slotRepo.addSlot(date, start, end, capacity, admin.getAccountId());
System.out.println("Slot added successfully!");
}
    }
