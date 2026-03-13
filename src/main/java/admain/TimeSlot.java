package admain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class TimeSlot {
    private int id;
    private ZonedDateTime start;
    private ZonedDateTime end;

    public TimeSlot(int id, ZonedDateTime start, ZonedDateTime end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public int getId() { return id; }
    public ZonedDateTime getStart() { return start; }
    public ZonedDateTime getEnd() { return end; }

    public int getDurationMinutes() {
        return (int) Duration.between(start, end).toMinutes();
    }

    @Override
    public String toString() {
        return "TimeSlot{id=" + id + ", start=" + start + ", end=" + end +
                ", duration=" + getDurationMinutes() + " minutes}";
    }
}