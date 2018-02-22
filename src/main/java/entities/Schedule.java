package entities;

import java.util.*;

public class Schedule {

    HashMap<Employee, List<SlotList>> employeesCalendar;
    int currentSlotId;

    public Schedule(HashMap<Employee, List<DaySlot>> agenda) {
        employeesCalendar = new HashMap<>();
        currentSlotId = 0;
        for (Employee e : agenda.keySet()) {
            employeesCalendar.put(e, createSlotAgenda(agenda.get(e)));
        }
    }

    private List<SlotList> createSlotAgenda(List<DaySlot> agenda) {
        Collections.sort(agenda);

        List<SlotList> slotAgenda = new ArrayList<>();
        int i = 0;
        //While there are agenda slots to initialize
        while (i < agenda.size()) {
            int beginSlotId = agenda.get(i).getId();
            if (currentSlotId < beginSlotId) currentSlotId = beginSlotId;
            //Initialize the new SlotList instance with the first slot
            LinkedHashMap<Integer, DaySlot> daySlotHashMap = new LinkedHashMap<>();
            daySlotHashMap.put(agenda.get(i).getId(), agenda.get(i));
            ++i;
            //While the following slots are of the same status, keep adding them to SlotList instance
            while (i < agenda.size() && agenda.get(i).getStatus().equals(agenda.get(i - 1).getStatus())) {
                if (currentSlotId < agenda.get(i).getId()) currentSlotId = agenda.get(i).getId();
                daySlotHashMap.put(agenda.get(i).getId(), agenda.get(i));
                ++i;
            }
            slotAgenda.add(new SlotList(daySlotHashMap));
        }
        return slotAgenda;
    }

    public boolean scheduleFeature(PlannedFeature pf, PlannedFeature lastPreviousPlannedFeature) {

        DaySlot lastPreviousFeatureEndSlot = null;
        if (lastPreviousPlannedFeature != null)
            lastPreviousFeatureEndSlot = getLastPreviousFeatureEndSlot(lastPreviousPlannedFeature);

        SlotList slotAgenda = getFirstAvailableSlot(pf, lastPreviousFeatureEndSlot);

        if (slotAgenda == null)
            return false;

        //FIXME used lastPreviousFeatureEndSlot always as a placeholder for algorithm simplicity
        if (lastPreviousFeatureEndSlot == null) {
            DaySlot beginSlot = slotAgenda.getDaySlot(slotAgenda.getBeginSlotId());
            lastPreviousFeatureEndSlot = new DaySlot(-1, beginSlot.getWeek(), beginSlot.getDayOfWeek() - 1, beginSlot.getBeginHour(), beginSlot.getBeginHour(), SlotStatus.Free);
        }
        updateAgenda(pf, slotAgenda, lastPreviousFeatureEndSlot);

        return true;
    }

    private void updateAgenda(PlannedFeature pf, SlotList slotAgenda, DaySlot lastPreviousFeatureEndSlot) {
        LinkedHashMap<Integer, DaySlot> previousAgenda = new LinkedHashMap<>();
        LinkedHashMap<Integer, DaySlot> thisAgenda = new LinkedHashMap<>();
        LinkedHashMap<Integer, DaySlot> laterAgenda = new LinkedHashMap<>();
        double remainingHours = pf.getFeature().getDuration();
        for (DaySlot daySlot: slotAgenda.getDaySlots().values()) {
            int cmp = daySlot.compareByDay(lastPreviousFeatureEndSlot);
            if (cmp < 0) {
                //If DaySlot is previous to end of previous feature
                previousAgenda.put(daySlot.getId(), daySlot);
            }
            else if (cmp == 0) {
                DaySlot prevDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                        daySlot.getBeginHour(), lastPreviousFeatureEndSlot.getEndHour(), SlotStatus.Free);
                //If last previous feature is ended today
                previousAgenda.put(currentSlotId, prevDaySlot);

                if (daySlot.getEndHour() - lastPreviousFeatureEndSlot.getEndHour() > 0) {
                    double hour = Math.min(lastPreviousFeatureEndSlot.getEndHour() + remainingHours,
                            daySlot.getEndHour());
                    DaySlot thisDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                            lastPreviousFeatureEndSlot.getEndHour(), hour, SlotStatus.Used);
                    //If after ending previous feature there are remaining work hours
                    thisAgenda.put(currentSlotId, thisDaySlot);
                    remainingHours -= (hour - lastPreviousFeatureEndSlot.getEndHour());
                    if (hour < daySlot.getEndHour()) {
                        DaySlot afterDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                hour, daySlot.getEndHour(), SlotStatus.Free);
                        //If feature is also finished the same day
                        laterAgenda.put(currentSlotId, afterDaySlot);
                    }

                }
            }
            else {
                if (remainingHours > 0) {
                    double hour = Math.min(daySlot.getBeginHour() + remainingHours, daySlot.getEndHour());
                    DaySlot thisDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                            daySlot.getBeginHour(), daySlot.getBeginHour() + hour, SlotStatus.Used);
                    thisAgenda.put(currentSlotId, thisDaySlot);
                    remainingHours -= (hour - daySlot.getBeginHour());
                    if (hour < daySlot.getEndHour()) {
                        DaySlot afterDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                hour, daySlot.getEndHour(), SlotStatus.Free);
                        //If feature is also finished the same day
                        laterAgenda.put(currentSlotId, afterDaySlot);
                    }
                } else {
                    laterAgenda.put(daySlot.getId(), daySlot);
                }
            }
        }
        //Delete old slotList
        List<SlotList> agenda = employeesCalendar.get(pf.getEmployee());
        int k = agenda.indexOf(slotAgenda);
        agenda.remove(k);
        //Add new 3 slotLists in order: after, this, before
        agenda.add(k, new SlotList(laterAgenda));
        agenda.add(k, new SlotList(thisAgenda));
        agenda.add(k, new SlotList(previousAgenda));

    }

    private SlotList getFirstAvailableSlot(PlannedFeature pf, DaySlot lastPreviousFeatureEndSlot) {
        List<SlotList> employeeAgenda = employeesCalendar.get(pf.getEmployee());
        int i = 0;
        while (i < employeeAgenda.size()) {
            if (employeeAgenda.get(i).isFeatureFit(pf.getFeature(), lastPreviousFeatureEndSlot)) {
                return employeeAgenda.get(i);
            }
            else ++i;
        }
        return null;
    }

    private DaySlot getLastPreviousFeatureEndSlot(PlannedFeature pf) {
        for (SlotList slotList : employeesCalendar.get(pf.getEmployee())) {
            DaySlot daySlot = slotList.getDaySlot(pf.getEndSlotId());
            if (daySlot != null) return daySlot;
        }
        return null;
    }

    @Override
    public String toString() {
        String s = "";
        for (Employee e : employeesCalendar.keySet()) {
            s += "Employee " + e.getName() + " schedule:\n";
            for (SlotList slotList : employeesCalendar.get(e)) {
                s += "\tSlot [" + slotList.getSlotStatus() + "]\n";
                for (DaySlot daySlot : slotList.getDaySlots().values()) {
                    s += "\t\t[" + daySlot.getId() + "] Week " + daySlot.getWeek() +  ", day " + daySlot.getDayOfWeek() + ", begins at " +
                            daySlot.getBeginHour() + " and ends at " + daySlot.getEndHour() + "\n";
                }
            }
        }
        return s;
    }

}
