package admain;

import java.time.LocalDateTime;

public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean available = true;

    public TimeSlot(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}