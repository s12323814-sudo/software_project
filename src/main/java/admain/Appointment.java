package admain;

public class Appointment {

    private int appointmentId;     // Primary Key
    private int userId;
    private int slotId;            // مهم للـ DB
    private TimeSlot timeSlot;     // optional (للـ JOIN)
    private int participants;
    private AppointmentStatus_y status;
    private AppointmentType_y type;
    private String username;
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }   //////////////////////////////////////
   
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

  

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getUserId() {
        return userId;
    }

    public int getSlotId() {
        return slotId;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public int getParticipants() {
        return participants;
    }

    public AppointmentStatus_y getStatus() {
        return status;
    }

    public AppointmentType_y getType() {
        return type;
    }

    public void setStatus(AppointmentStatus_y status) {
        this.status = status;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    
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