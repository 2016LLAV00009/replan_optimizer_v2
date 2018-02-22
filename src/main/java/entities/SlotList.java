package entities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SlotList {

    private LinkedHashMap<Integer, DaySlot> daySlots;
    private int beginSlotId;
    private int endSlotId;
    private SlotStatus slotStatus;

    public SlotList(LinkedHashMap<Integer, DaySlot> daySlots) {
        this.daySlots = daySlots;
        this.beginSlotId = (Integer) daySlots.keySet().toArray()[0];
        this.endSlotId = (Integer) daySlots.keySet().toArray()[daySlots.size()-1];
        this.slotStatus = daySlots.get(beginSlotId).getStatus();
    }

    public LinkedHashMap<Integer, DaySlot> getDaySlots() { return daySlots; }
    public void setDaySlots(LinkedHashMap<Integer, DaySlot> daySlots) { this.daySlots = daySlots; }

    public int getBeginSlotId() { return beginSlotId; }
    public void setBeginSlotId(int beginSlotId) { this.beginSlotId = beginSlotId; }

    public int getEndSlotId() { return endSlotId; }
    public void setEndSlotId(int endSlotId) { this.endSlotId = endSlotId; }

    public SlotStatus getSlotStatus() { return slotStatus; }
    public void setSlotStatus(SlotStatus slotStatus) { this.slotStatus = slotStatus; }

    public DaySlot getDaySlot(int id) {
        if (daySlots.containsKey(id)) return daySlots.get(id);
        else return null;
    }

    public double getTotalDuration() {
        double duration = 0.0;
        for (DaySlot daySlot : daySlots.values())
            duration += daySlot.getDuration();
        return duration;
    }

    public boolean isFeatureFit(Feature feature, DaySlot lastPreviousFeatureEndSlot) {
        if (slotStatus.equals(SlotStatus.Used)) return false;
        DaySlot endDaySlot = daySlots.get(endSlotId);
        DaySlot beginDaySlot = daySlots.get(beginSlotId);
        //If any previous feature is planned after this slot, feature can't be planned
        if (lastPreviousFeatureEndSlot != null &&
                lastPreviousFeatureEndSlot.compareTo(endDaySlot) >= 0)
            return false;
        //Otherwise, we check if feature duration fits in this slot duration
        else {
            double freeTime = 0.0;
            //If there is no previous feature or it has ended before the beginning of this slot
            if (lastPreviousFeatureEndSlot == null ||
                    lastPreviousFeatureEndSlot != null &&
                    lastPreviousFeatureEndSlot.compareTo(beginDaySlot) <= 0)
                freeTime = getTotalDuration();
            //Otherwise
            else {
                for (DaySlot slot : daySlots.values()) {
                    int cmp = slot.compareByDay(lastPreviousFeatureEndSlot);
                    if (cmp == 0)
                        freeTime += slot.getEndHour() - lastPreviousFeatureEndSlot.getEndHour();
                    else if (cmp > 0)
                        freeTime += slot.getDuration();
                }
            }
            return freeTime >= feature.getDuration();
        }
    }

}
