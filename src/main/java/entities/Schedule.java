package entities;

import java.util.*;

public class Schedule {

    HashMap<Employee, List<SlotList>> employeesCalendar;
    HashMap<Employee, Integer> currentSlotIds;

    public Schedule(HashMap<Employee, List<DaySlot>> agenda) {
        employeesCalendar = new HashMap<>();
        currentSlotIds = new HashMap<>();
        for (Employee e : agenda.keySet()) {
            int currentSlotId = createSlotAgenda(e, agenda.get(e));
            currentSlotIds.put(e, currentSlotId);
        }
    }

    private int createSlotAgenda(Employee e, List<DaySlot> agenda) {
        Collections.sort(agenda);
        int currentSlotId = 0;
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
        employeesCalendar.put(e, slotAgenda);
        return currentSlotId;
    }

    public boolean scheduleFeature(PlannedFeature pf, List<PlannedFeature> previousFeatures) {
        return scheduleFeature(pf, getLatestPlannedFeature(previousFeatures));
    }

    private DaySlot getLatestPlannedFeature(List<PlannedFeature> previousFeatures) {
        if (previousFeatures == null || previousFeatures.isEmpty())
            return null;
        DaySlot last = getPlannedFeatureEndSlot(previousFeatures.get(0));
        for (int i = 1; i < previousFeatures.size(); ++i) {
            DaySlot current = getPlannedFeatureEndSlot(previousFeatures.get(i));
            if (last.compareTo(current) < 0) {
                last = current;
            }
        }
        return last;
    }

    private boolean scheduleFeature(PlannedFeature pf, DaySlot lastPreviousFeatureEndSlot) {

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
        int currentSlotId = currentSlotIds.get(pf.getEmployee());
        for (DaySlot daySlot: slotAgenda.getDaySlots().values()) {
            int cmp = daySlot.compareByDay(lastPreviousFeatureEndSlot);
            if (cmp < 0) {
                //If DaySlot is previous to end of previous feature
                daySlot.setStatus(SlotStatus.Free);
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
                    double hour = Math.min(remainingHours, daySlot.getDuration());
                    remainingHours -= hour;
                    if (daySlot.getBeginHour() + hour < daySlot.getEndHour()) {
                        DaySlot thisDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                daySlot.getBeginHour(), daySlot.getBeginHour() + hour, SlotStatus.Used);
                        thisAgenda.put(currentSlotId, thisDaySlot);

                        DaySlot afterDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                daySlot.getBeginHour() + hour, daySlot.getEndHour(), SlotStatus.Free);
                        //If feature is also finished the same day
                        laterAgenda.put(currentSlotId, afterDaySlot);
                    } else {
                        daySlot.setStatus(SlotStatus.Used);
                        thisAgenda.put(daySlot.getId(), daySlot);
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
        if (laterAgenda.size() > 0) agenda.add(k, new SlotList(laterAgenda));
        if (thisAgenda.size() > 0) agenda.add(k, new SlotList(thisAgenda));
        if (previousAgenda.size() > 0)  agenda.add(k, new SlotList(previousAgenda));
        currentSlotIds.put(pf.getEmployee(), currentSlotId);
        //pf.setSlotIds((List<Integer>) thisAgenda.keySet());
        List<Integer> daySlotIds = new ArrayList<>();
        for (Integer i : thisAgenda.keySet()) {
            daySlotIds.add(i);
        }
        pf.setSlotIds(daySlotIds);
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

    private DaySlot getPlannedFeatureEndSlot(PlannedFeature pf) {
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
