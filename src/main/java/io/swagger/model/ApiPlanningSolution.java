package io.swagger.model;

import entities.Employee;
import entities.Feature;
import entities.PlannedFeature;
import logic.NextReleaseProblem;
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

    private double priorityQuality;
    private double performanceQuality;
    private double similarityQuality;
    private double globalQuality;
    private List<Employee> employees;

    /* --- CONSTRUCTORS --- */
    public ApiPlanningSolution() {
        employees = new ArrayList<>();
    }

    public ApiPlanningSolution(PlanningSolution solution) {
        employees = solution.getEmployees();
        priorityQuality = solution.getObjective(NextReleaseProblem.INDEX_PRIORITY_OBJECTIVE);
        performanceQuality = solution.getObjective(NextReleaseProblem.INDEX_SUBOBJECTIVES);
        similarityQuality = solution.getObjective(NextReleaseProblem.INDEX_SIMILARITY_OBJECTIVE);
        globalQuality = solution.getProblem().getSolutionQuality().getAttribute(solution);
    }

    /* --- GETTERS / SETTERS --- */

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public double getPriorityQuality() {
        return priorityQuality;
    }

    public void setPriorityQuality(double priorityQuality) {
        this.priorityQuality = priorityQuality;
    }

    public double getPerformanceQuality() {
        return performanceQuality;
    }

    public void setPerformanceQuality(double performanceQuality) {
        this.performanceQuality = performanceQuality;
    }

    public double getSimilarityQuality() {
        return similarityQuality;
    }

    public void setSimilarityQuality(double similarityQuality) {
        this.similarityQuality = similarityQuality;
    }

    public double getGlobalQuality() {
        return globalQuality;
    }

    public void setGlobalQuality(double globalQuality) {
        this.globalQuality = globalQuality;
    }
}
