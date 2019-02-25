package entities;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Describes an employee who can implement a feature
 * @author Vavou
 *
 */
public class Employee {

	private String name;
	private HashMap<String, Double> skills;
	private List<DaySlot> calendar;
	
	/* --- GETTERS / SETTERS --- */

	@ApiModelProperty(value = "") public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@ApiModelProperty(value = "") public HashMap<String, Double> getSkills() {
		return skills;
	}
	public void setSkills(HashMap<String, Double> skills) {
		this.skills = skills;
	}

	@ApiModelProperty(value = "") public List<DaySlot> getCalendar() { return calendar; }
	public void setCalendar(List<DaySlot> calendar) { this.calendar = calendar; }

	/* --- CONSTRUCTORS --- */

	public Employee() {
        skills = new HashMap<>();
    }

	public Employee(String name, HashMap<String, Double> skills) {
		this.name = name;
		this.skills = skills == null ? new HashMap<>() : skills;
	}

	public Employee(String name, HashMap<String, Double> skills, List<DaySlot> calendar) {
		this.name = name;
		this.skills = skills == null ? new HashMap<>() : skills;
		this.calendar = calendar;
	}

	/* --- OTHER --- */
	@Override 
	public String toString() {
		List<String> skillNames = new ArrayList<>();
		for (String s : getSkills().keySet())
			skillNames.add(s);

		return String.format("%s. Skills: [%s].", getName(), String.join(", ", skillNames));
	}

	@Override 
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;

		Employee other = (Employee) obj;
		
		return this.getName().equals(other.getName());
	}
	
	@Override
	public int hashCode() {
		return getName().length();
	}

    public List<DaySlot> copyCalendar() {
		List<DaySlot> copy = new ArrayList<>();
		for (DaySlot daySlot : calendar) {
			copy.add(new DaySlot(daySlot));
		}
		return copy;
    }
}
