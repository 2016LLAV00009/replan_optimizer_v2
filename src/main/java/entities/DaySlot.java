package entities;

public class DaySlot implements Comparable<DaySlot> {


    private int id;
    private int week;
    private int dayOfWeek;
    private double beginHour;
    private double endHour;
    private SlotStatus status;

    public DaySlot(int id, int week, int dayOfWeek, double beginHour, double endHour,
                   SlotStatus status) {
        this.id = id;
        this.week = week;
        this.dayOfWeek = dayOfWeek;
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWeek() { return week; }
    public void setWeek(int week) { this.week = week; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public double getBeginHour() { return beginHour; }
    public void setBeginHour(double beginHour) { this.beginHour = beginHour; }

    public double getEndHour() { return endHour; }
    public void setEndHour(double endHour) { this.endHour = endHour; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }

    public double getDuration() {
        return endHour - beginHour;
    }

    public int compareByDay(DaySlot daySlot) {
        return (this.week * 7 + this.dayOfWeek) - (daySlot.week * 7 + daySlot.dayOfWeek);
    }

    @Override
    public int compareTo(DaySlot daySlot) {
        return (int) (this.week * 7 * 24 * 60
                        + this.dayOfWeek * 24 * 60
                        + this.beginHour * 60) -
                (int) (daySlot.week * 7 * 24 * 60
                        + daySlot.dayOfWeek * 24 * 60
                        + daySlot.beginHour * 60);
    }

    @Override
    public String toString() {
        return "[SLOT] week: " + week + " | day: " + dayOfWeek + " | beginHour: " + beginHour
                + " | endHour: " + endHour + " | status: " + status;
    }

}
