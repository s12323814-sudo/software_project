package admain;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class SlotService_y {

    private AppointmentRepository_y appointmentRepo;
    private SlotRepository_y slotRepo;
    private static Scanner sc = new Scanner(System.in);
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
    public boolean bookAppointment(int userId, int slotId, int participants) throws SQLException {

        if (participants <= 0) return false;

        AppointmentSlot_y slot = slotRepo.findById(slotId);

        if (slot == null) return false;

        int remaining = slot.getMaxCapacity() - slot.getBookedCount();

        if (participants > remaining) return false;

        return appointmentRepo.book(userId, slotId, participants);
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
                           int capacity, int adminId, AppointmentType_y type) {

        if (capacity <= 0) return false;

        return slotRepo.addSlot(date, start, end, capacity, adminId, type);
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
        public void addSlotInteractive(Account_y admin) {
            if (admin == null || !admin.isAdmin()) {
                System.out.println("Admin only!");
                return;
            }

            try {
                System.out.print("Enter date (YYYY-MM-DD): ");
                LocalDate date = LocalDate.parse(sc.nextLine());

                System.out.print("Enter start time (HH:MM): ");
                LocalTime start = LocalTime.parse(sc.nextLine());

                System.out.print("Enter end time (HH:MM): ");
                LocalTime end = LocalTime.parse(sc.nextLine());

                System.out.print("Enter max capacity: ");
                int capacity = Integer.parseInt(sc.nextLine());

                System.out.println("Choose Appointment Type:");
                AppointmentType_y[] types = AppointmentType_y.values();
                for (int i = 0; i < types.length; i++) {
                    System.out.println((i + 1) + "- " + types[i]);
                }

                int typeChoice = Integer.parseInt(sc.nextLine());
                if (typeChoice < 1 || typeChoice > types.length) {
                    System.out.println("Invalid type choice!");
                    return;
                }

                AppointmentType_y selectedType = types[typeChoice - 1];

                slotRepo.addSlot(date, start, end, capacity, admin.getAccountId(), selectedType);
                System.out.println("Slot added successfully!");

            } catch (Exception e) {
                System.out.println("Error adding slot: " + e.getMessage());
            }
        }
    }
