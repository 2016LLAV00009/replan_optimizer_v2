package entities;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Describes a feature in a planning
 * Contains 
 * - the feature to do
 * - the employee in charge of the feature
 * - the begin hour in the planning
 * - the end hour in the planning
 * @author Vavou
 *
 */
public class PlannedFeature {
	
	/* --- Attributes --- */
	/**
	 * A frozen feature will NOT be replanned.
	 */
	private boolean frozen;

	/**
	 * The employee who will do the feature
	 */
	@SerializedName("resource")
	private Employee employee;
	
	/**
	 * The feature to do
	 */
	private Feature feature;

	private List<DaySlot> daySlots;

	private Calendar beginTime;
	private Calendar endTime;

	
	/* --- Getters and setters --- */

	@ApiModelProperty(value="")
	public List<DaySlot> getDaySlots() { return this.daySlots; }
	public void setDaySlots(List<DaySlot> daySlots) { this.daySlots = daySlots; }

	@ApiModelProperty(value = "")
	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	@ApiModelProperty(value = "")
	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	@ApiModelProperty(value = "")
	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public void setBeginTime(Calendar beginTime) {
		this.beginTime = beginTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public Calendar getBeginTime() {
		return this.beginTime;
	}

	public Calendar getEndTime() {
		return this.endTime;
	}

	/* --- Constructors --- */

    public PlannedFeature() {}

	/**
	 * Construct a planned feature
	 * @param feature the feature to plan
	 * @param employee the employee who realize the feature
	 */
	public PlannedFeature(Feature feature, Employee employee) {
		this.feature = feature;
		this.employee = employee;
		this.daySlots = new ArrayList<>();
	}
	
	/**
	 * Copy constructor
	 * @param origin the object to copy
	 */
	public PlannedFeature(PlannedFeature origin) {
		this.employee = origin.getEmployee();
		this.feature = origin.getFeature();
		this.daySlots = origin.getDaySlots();
	}

	/* --- Methods --- */
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;

		PlannedFeature other = (PlannedFeature) obj;

		return other.getFeature().equals(this.getFeature()) &&
				other.getEmployee().equals(this.getEmployee()) &&
				other.getDaySlots().equals(this.getDaySlots());
	}
	
	@Override
	public int hashCode() {
		return feature.hashCode() + employee.hashCode() + daySlots.hashCode();
	}
	
	@Override
	public String toString() {
		return String.valueOf(getFeature()) + " done by " + getEmployee() +
				" from " + daySlots.get(0).getBeginTime() + " to " + daySlots.get(daySlots.size()-1).getEndTime();
	}
}
