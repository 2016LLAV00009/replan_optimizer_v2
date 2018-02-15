package entities;

import java.util.Calendar;

public class DaySlot implements Comparable<DaySlot> {

    private int slotId;
    private double beginHour;
    private double endHour;
    private SlotStatus status;

    public DaySlot(int slotId,
                   double beginHour,
                   double endHour,
                   SlotStatus status) {
        this.slotId = slotId;
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.status = status;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public double getDuration() {
        return endHour - beginHour;
    }


    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    @Override
    public int compareTo(DaySlot slot) {
        return (int) this.beginHour*60 - (int) slot.beginHour*60;
    }

}
