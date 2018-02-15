package entities;

import java.util.*;

public class Slot {
	
	private List<DaySlot> daySlots;
	private Calendar beginTime;
	private Calendar endTime;
	private SlotStatus status;

	public Slot(List<DaySlot> scheduleSlots, SlotStatus status) {
		this.daySlots = scheduleSlots;
		this.beginTime = scheduleSlots.get(0).getBeginTime();
		this.endTime = scheduleSlots.get(scheduleSlots.size() -1).getEndTime();
		this.status = status;
	}
	public boolean isFeatureFit(PlannedFeature pf) {
		if (!status.equals(SlotStatus.FREE)) return false;
		if (getEndTime().before(pf.getBeginTime())) return false;

		double leftTime = 0.0;
		int i = 0;
		while (pf.getBeginTime().before(daySlots.get(i).getBeginTime())) ++i;

		for (; i < daySlots.size(); ++i) {
			DaySlot ds = daySlots.get(i);
			long wholeDayDuration = ds.getDuration();
			long endDayDuration = ds.getEndTime().getTimeInMillis() - pf.getBeginTime().getTimeInMillis();
			leftTime += Math.min(wholeDayDuration, endDayDuration);
		}

		return leftTime >= pf.getFeature().getDuration() * 3600000;

	}
	public double getTotalDuration() {
		double sum = 0.0;
		for (DaySlot ws : daySlots) sum += ws.getDuration();
		return sum;
	}
	public List<DaySlot> getDaySlots() {
		return daySlots;
	}

	public Calendar getBeginTime() {
		return beginTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}
}
