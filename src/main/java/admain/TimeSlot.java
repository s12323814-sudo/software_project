package admain;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeSlot {

    private int id; // معرف slot في DB
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeSlot(int id, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    // Duration محسوبة بالدقائق
    public int getDurationMinutes() {
        return (int) Duration.between(start, end).toMinutes();
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                ", duration=" + getDurationMinutes() + " minutes" +
                '}';
    }
}