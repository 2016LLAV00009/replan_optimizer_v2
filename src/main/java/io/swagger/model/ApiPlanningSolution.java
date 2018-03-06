package io.swagger.model;

import entities.Employee;
import entities.Feature;
import entities.PlannedFeature;
import logic.PlanningSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience data class for returning data from the API call.
 * Don't try to use {@link PlanningSolution} as it extends a class implementing Serializable,
 * which makes it hard to serialize only the fields relevant to the API.
 *
 * @author kredes
 */
public class ApiPlanningSolution {

    private List<Employee> employees;

    /* --- CONSTRUCTORS --- */
    public ApiPlanningSolution() {
        employees = new ArrayList<>();
    }

    public ApiPlanningSolution(PlanningSolution solution) {
        employees = solution.getEmployees();
    }



    /* --- GETTERS / SETTERS --- */

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    /*public List<PlannedFeature> getJobs() {
        return jobs;
    }

    public void setJobs(List<PlannedFeature> jobs) {
        this.jobs = jobs;
    }

    public PlannedFeature findJobOf(Feature f) {
        for (PlannedFeature pf : jobs)
            if (pf.getFeature().equals(f))
                return pf;

        return null;
    }*/
}
