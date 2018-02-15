package entities;

import java.util.*;

public class NewSchedule {

    private Employee employee;
    private Set<PlannedFeature> plannedFeatures;

    // The number of hours left this employee has for the whole release
    private long workTimeLeft;
    // The slot calendar
    private List<Slot> scheduleSlots;

    public NewSchedule() {

	}

	/**
	 *
	 * @param employee
	 * @param daySlots MUST be ordered
	 */
	public NewSchedule(Employee employee, List<DaySlot> daySlots) {
		this.employee = employee;
		this.scheduleSlots = new ArrayList<>();

		List<DaySlot> currentStatusDaySlots = new ArrayList<>();
		daySlots.add(daySlots.get(0));
		long workTimeLeft =
		for (int i = 1; i < daySlots.size(); ++i) {
			if (daySlots.get(i).getStatus().equals(daySlots.get(i-1).getStatus()))
				currentStatusDaySlots.add(daySlots.get(i));
			else {
				scheduleSlots.add(new Slot(currentStatusDaySlots, daySlots.get(i-1).getStatus()));
				currentStatusDaySlots = new ArrayList<>();
			}
		}

		//Creates a Slot with all the daily slots
		/*HashMap<Integer, DaySlot> days = new HashMap<>();
		Collections.sort(daySlots);
		for (DaySlot s : daySlots) {
			days.put(0, s);
		}*/
		// Fixme initialize slots according to whatever takes
		// Slot s = new Slot(days,);
		//this.scheduleSlots.add(s);

		//Calculates the left hours
		/*totalHoursLeft = 0;
		for (DaySlot slot : daySlots)
			if (slot.getStatus().equals(SlotStatus.FREE))
				totalHoursLeft += slot.getDuration();*/

		//Initializes planning features data structure
		plannedFeatures = new HashSet<>();
	}
    
    public NewSchedule(NewSchedule old) {
		this();
		this.scheduleSlots = old.scheduleSlots;
		this.employee = old.employee;
		totalHoursLeft = old.getTotalHoursLeft();
		plannedFeatures = old.plannedFeatures;
	}
    
    private void printSlots() {
    	//TODO print new schedule
	}
    
    /* --- PUBLIC --- */

    public boolean scheduleFeature(PlannedFeature pf) {
        return scheduleFeature(pf, true);
    }

    /**
     * Tries to schedule a PlannedFeature in the first available week
     * @param pf the PlannedFeature to be scheduled
     * @return a boolean indicating whether the PlannedFeature could be scheduled
     */
    public boolean scheduleFeature(PlannedFeature pf, boolean adjustHours) {
    	    	
    	double featureHoursLeft = pf.getFeature().getDuration();
    	
        // Not enough hours left for this feature in the iteration
        if (totalHoursLeft < featureHoursLeft)
            return false;
    	
        Slot slot = getFirstAvailableSlot(pf);
    	if (slot == null) 
    		return false;
    	
    	if (pf.getBeginTime().after(slot.getBeginTime())) pf.setBeginTime(pf.getBeginTime());
    	else pf.setBeginTime(slot.getBeginTime());
    	Calendar currentTime = pf.getBeginTime();

    	Collection<DaySlot> weeks = slot.getDaySlots().values();
    	Iterator iterator = weeks.iterator();
    	DaySlot day = (DaySlot) iterator.next();

    	while (iterator.hasNext() &&
				day.getBeginTime().before(pf.getBeginTime()) &&
				pf.getBeginTime().after(day.getEndTime())) {
    		day = (DaySlot) iterator.next();
    	}
    	
    	while (featureHoursLeft > 0.0) {

    		double doneHours = Math.min(featureHoursLeft, day.getDuration());
    		doneHours = Math.min(doneHours, hoursBetween(currentTime, day.getEndTime()));

    		featureHoursLeft -= doneHours;
    		totalHoursLeft -= doneHours;
    		currentTime.add(Calendar.HOUR_OF_DAY, (int) Math.floor(doneHours));
    		currentTime.add(Calendar.MINUTE, (int) (doneHours - Math.floor(doneHours) * 60));

    		//FIXME:
			// 1) day has been filled
			// 2) day has been filled, but feature continues
			// 3) day has not been filled

    		if (featureHoursLeft > 0.0 && iterator.hasNext()) {
    			day.setStatus(SlotStatus.USED);
    			day = (DaySlot) iterator.next();
    			currentTime = day.getBeginTime();
			} else if (featureHoursLeft == 0.0) day.setStatus(SlotStatus.USED);
    	}
    	
    	pf.setEndTime(currentTime);
    	
    	plannedFeatures.add(pf);
    	    	    	 
        try {
			updateSlots(pf, slot);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return true;
    	
    }

	public double hoursBetween(Calendar d1, Calendar d2){
		return (d1.getTimeInMillis() - d2.getTimeInMillis()) / 3600000;
	}

	private void updateSlots(PlannedFeature pf, Slot slot) throws Exception {
		
		/*int i = slots.indexOf(slot);
		
		HashMap<Integer, DaySlot> beforeWeekSlots = new HashMap<>();
		HashMap<Integer, DaySlot> afterWeekSlots = new HashMap<>();
		
		List<Integer> removedWeeks = new ArrayList<>();
								
		for (Integer week : slot.getWeekSlots().keySet()) {
			DaySlot daySlot = slot.getWeekSlots().get(week);
			
			if (daySlot.getEndHour() <= pf.getBeginHour()) {
				beforeWeekSlots.put(week, new DaySlot(daySlot.getBeginHour(), daySlot.getEndHour(), daySlot.getDuration()));
			} 
			
			else if (daySlot.getBeginHour() >= pf.getEndHour()) {
				afterWeekSlots.put(week, new DaySlot(daySlot.getBeginHour(), daySlot.getEndHour(), daySlot.getDuration()));
			} 
			
			else if (daySlot.getBeginHour() < pf.getBeginHour()
					&& daySlot.getEndHour() <= pf.getEndHour()) {
				double duration = daySlot.getDuration() - (daySlot.getEndHour() - pf.getBeginHour());
				if (duration > 0.0)
					beforeWeekSlots.put(week, new DaySlot(daySlot.getBeginHour(), pf.getBeginHour(), duration));
			} 
			
			else if (daySlot.getBeginHour() >= pf.getBeginHour()
					&& daySlot.getEndHour() > pf.getEndHour()) {
				double duration = daySlot.getDuration() - (pf.getEndHour() - daySlot.getBeginHour());
				if (duration > 0.0)
					afterWeekSlots.put(week, new DaySlot(pf.getEndHour(), daySlot.getEndHour(), duration));
			} 
			
			else if (daySlot.getBeginHour() < pf.getBeginHour()
					&& daySlot.getEndHour() > pf.getEndHour()) {
				double duration = Math.max(0, daySlot.getDuration() - pf.getFeature().getDuration());
				if (duration > 0.0) {
					beforeWeekSlots.put(week, new DaySlot(daySlot.getBeginHour(), pf.getBeginHour(), Math.min(duration, pf.getBeginHour() - daySlot.getBeginHour())));
					afterWeekSlots.put(week, new DaySlot(pf.getEndHour(), daySlot.getEndHour(), Math.min(duration, daySlot.getEndHour() - pf.getEndHour())));
				}
			} 
			else	removedWeeks.add(week);
			//else throw new Exception("Error in Scheduling algorithm - review slot redistribution");
			
		}
		
		for (Integer w : removedWeeks) removeWeek(w);
		
		slots.remove(i);
		if (!afterWeekSlots.isEmpty()) {
			slots.add(i, new Slot(afterWeekSlots));
		}
		if (!beforeWeekSlots.isEmpty()) {
			slots.add(i, new Slot(beforeWeekSlots));
		}
		
		this.beginHour = Math.min(beginHour, pf.getBeginHour());
		this.endHour = Math.max(endHour, pf.getEndHour());
		//System.out.println("After scheduling:");
		//printSlots();*/
		//FIXME Check how to update new slot structure
		
	}

	private void removeWeek(Integer week) {
		for (Slot s : slots) {
			s.getWeekSlots().remove(week);
		}
	}

	private Slot getFirstAvailableSlot(PlannedFeature pf) {
		int i = 0;
		while (i < scheduleSlots.size()) {
			if (scheduleSlots.get(i).isFeatureFit(pf)) {
				return scheduleSlots.get(i);
			}
		}
		return null;
	}

    public boolean isEmpty() { return plannedFeatures.isEmpty(); }

    public boolean contains(PlannedFeature pf) { return plannedFeatures.contains(pf); }

    public Employee getEmployee() { return employee; }

    public List<PlannedFeature> getPlannedFeatures() {
        return new ArrayList<>(plannedFeatures);
    }

    public double getTotalHoursLeft() { return totalHoursLeft; }

	public List<Slot> getScheduleSlots() {
		return scheduleSlots;
	}

	public void setScheduleSlots(List<Slot> scheduleSlots) {
		this.scheduleSlots = scheduleSlots;
	}

	@Override
    public String toString() {
        double availability = employee.getWeekAvailability();
        double hours = availability * nbWeeks - totalHoursLeft;

        return String.format(
                "%f hours planned over %d weeks with an availability of %f hours",
                hours, nbWeeks, availability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewSchedule schedule = (NewSchedule) o;

        if (Double.compare(schedule.totalHoursLeft, totalHoursLeft) != 0) return false;
        //if (nbWeeks != schedule.nbWeeks) return false;
        if (!employee.equals(schedule.employee)) return false;
        return plannedFeatures != null ? plannedFeatures.equals(schedule.plannedFeatures) : schedule.plannedFeatures == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = 31 + employee.hashCode();
        result = 31 * result + (plannedFeatures != null ? plannedFeatures.hashCode() : 0);
        temp = Double.doubleToLongBits(totalHoursLeft);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        //result = 31 * result + nbWeeks;
        return result;
    }
	
}
