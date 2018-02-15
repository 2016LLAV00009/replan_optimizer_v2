/**
 * @author Vavou
 */
package logic;

import entities.*;
import entities.parameters.AlgorithmParameters;
import entities.parameters.EvaluationParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO: The Frozen Features functionality needs to be remade to adapt it to the NewSchedule class
/**
 * A class representing a Next Release problem.
 * Objectives:
 * <ul>
 *     <li>0: Maximize the priority score</li>
 *     <li>1: Minimize endDate</li>
 * </ul>;
 */
public class NextReleaseProblem extends AbstractGenericProblem<PlanningSolution> implements ConstrainedProblem<PlanningSolution> {

	private static final long serialVersionUID = 3302475694747789178L; // Generated Id

	private static final Logger logger = LoggerFactory.getLogger(NextReleaseProblem.class);

	// PROBLEM
	private List<Feature> features;
	private List<Employee> employees;
	private List<DaySlot> scheduleSlots;
	private double replanHour; 		// The hour of the requested replan
	private int nbWeeks; 			// The number of weeks of the iteration
	private double nbHoursByWeek; 	// The number of worked hours by week
	private AlgorithmParameters algorithmParameters;
	private EvaluationParameters evaluationParameters;

	// SOLUTION
	private NumberOfViolatedConstraints<PlanningSolution> numberOfViolatedConstraints;
	private OverallConstraintViolation<PlanningSolution> overallConstraintViolation;
	private SolutionQuality solutionQuality;
	private Map<Employee, NewSchedule> planning;
	private double precedenceConstraintOverall; //The overall of a violated constraint
	private double worstScore; //The priority score if there is no planned feature
	private double worstEndDate; //The worst end date, if there is no planned feature

	// GETTERS / SETTERS
	public double getReplanHour() {
		return replanHour;
	}
	public void setReplanHour(double replanHour) {
		this.replanHour = replanHour;
	}
	public void setScheduleSlots(List<DaySlot> scheduleSlots) { this.scheduleSlots = scheduleSlots; }
	public List<DaySlot> getScheduleSlots() { return scheduleSlots; }
	public List<Feature> getFeatures() {
		return features;
	}
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	public int getNbWeeks() {
		return nbWeeks;
	}
	public double getNbHoursByWeek() {
		return nbHoursByWeek;
	}
	public NumberOfViolatedConstraints<PlanningSolution> getNumberOfViolatedConstraints() {
		return numberOfViolatedConstraints;
	}
	public List<Employee> getSkilledEmployees(List<Skill> reqSkills) {
		ArrayList<Employee> skilledEmployees = new ArrayList<>();
		for (Employee employee : employees)
			if(employee.getSkills().containsAll(reqSkills))
				skilledEmployees.add(employee);
		return skilledEmployees;
	}
	public List<Employee> getEmployees() {
		return employees;
	}
	public double getWorstScore() {
		return worstScore;
	}

	public AlgorithmParameters getAlgorithmParameters() { return algorithmParameters; }
	public void setAlgorithmParameters(AlgorithmParameters algorithmParameters) { this.algorithmParameters = algorithmParameters; }

	public EvaluationParameters getEvaluationParameters() { return evaluationParameters; }
	public void setEvaluationParameters(EvaluationParameters evaluationParameters) { this.evaluationParameters = evaluationParameters;}
	
	// Constructor (empty)
	public NextReleaseProblem() {
		setName("Next Release Problem");
		setNumberOfVariables(1);
		setNumberOfObjectives(4);
		features = new ArrayList<>();
		numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
		overallConstraintViolation = new OverallConstraintViolation<>();
		solutionQuality = new SolutionQuality();
		algorithmParameters = new AlgorithmParameters(SolverNRP.AlgorithmType.NSGAII);
		evaluationParameters = new EvaluationParameters();
	}

	// Constructor (normal)
	public NextReleaseProblem(List<Feature> features, List<Employee> employees,
							  int nbWeeks, double nbHoursPerWeek) {
		this();
		this.employees = employees;
		this.nbWeeks = nbWeeks;
		this.nbHoursByWeek = nbHoursPerWeek;

		// TODO: If a feature is not included because 1. lack of skills or 2. the dependee is not included; this information should be noted somewhere and send back to the controller once the plan is produced).
		// checks that features can be satisfied by the skills of the resources and the dependencies are included
		for (Feature feature : features)
			if (getSkilledEmployees(feature.getRequiredSkills()).size() > 0) // 1.
				if (features.containsAll(feature.getPreviousFeatures())) // 2.
					this.features.add(feature);

		worstEndDate = nbWeeks * nbHoursByWeek;
		replanHour = 0.0;

		initializeSchedule();
		initializeWorstScore();
		initializeNumberOfConstraint();
	}

	// Constructor (normal)
	public NextReleaseProblem(List<Feature> features, List<Employee> employees, List<DaySlot> scheduleSlots,
							  int nbWeeks, double nbHoursPerWeek) {
		this();
		this.employees = employees;
		this.nbWeeks = nbWeeks;
		this.nbHoursByWeek = nbHoursPerWeek;
		this.scheduleSlots = scheduleSlots;

		// TODO: If a feature is not included because 1. lack of skills or 2. the dependee is not included; this information should be noted somewhere and send back to the controller once the plan is produced).
		// checks that features can be satisfied by the skills of the resources and the dependencies are included
		for (Feature feature : features)
			if (getSkilledEmployees(feature.getRequiredSkills()).size() > 0) // 1.
				if (features.containsAll(feature.getPreviousFeatures())) // 2.
					this.features.add(feature);
		
		worstEndDate = nbWeeks * nbHoursByWeek;
		replanHour = 0.0;

		initializeSchedule();
		initializeWorstScore();
		initializeNumberOfConstraint();
	}

	private void initializeSchedule() {
		planning = new HashMap<>();
		for (Employee e : employees) {
			List<DaySlot> slots = new ArrayList<>();
			for (DaySlot slot : scheduleSlots) {
				if (slot.getResourceId().equals(e.getName())) slots.add(slot);
			}
			NewSchedule schedule = new NewSchedule(e, nbWeeks, nbHoursByWeek, slots);
			planning.put(e, schedule);
		}
	}

	public void setTag(String name) {
		setName(name);
	}
	
	// Constructor (for replanning)
	public NextReleaseProblem(List<Feature> features, List<Employee> employees, List<DaySlot> scheduleSlots,
								int nbWeeks, double nbHoursPerWeek, double replanHour) {
		this(features, employees, scheduleSlots, nbWeeks, nbHoursPerWeek);
		this.replanHour = replanHour;
		
	}

	// Copy constructor
	public NextReleaseProblem(NextReleaseProblem origin) {
		this(origin.getFeatures(), origin.getEmployees(), origin.getScheduleSlots(), origin.getNbWeeks(), origin.getNbHoursByWeek());
	}

	/* ------------ */

	// Initializes the worst score
	private void initializeWorstScore() {
		worstScore = 0.0;
		for (Feature feature : features)
			worstScore += feature.getPriority().getScore();
	}

	// Initializes the number of constraints for the problem
	private void initializeNumberOfConstraint() {
		int numberOfConstraints = 0;

		// 1 for each dependency
		for (Feature feature : features)
			numberOfConstraints += feature.getPreviousFeatures().size();

		precedenceConstraintOverall = 1.0 / numberOfConstraints;

		// 1 for passing the deadline of the release
		numberOfConstraints++;

		setNumberOfConstraints(numberOfConstraints);
	}

	@Override
	public PlanningSolution createSolution() {
		return new PlanningSolution(this);
	}

	@Override
	public void evaluate(PlanningSolution solution) {
		List<PlannedFeature> plannedFeatures = solution.getPlannedFeatures();
		
        for (PlannedFeature currentPlannedFeature : plannedFeatures) {
            //computeHours(solution, currentPlannedFeature);
			Employee employee = currentPlannedFeature.getEmployee();
			NewSchedule employeeNewSchedule = planning.getOrDefault(employee, new NewSchedule(employee, nbWeeks, nbHoursByWeek));
			
			if (!employeeNewSchedule.contains(currentPlannedFeature)) {
                if (!employeeNewSchedule.scheduleFeature(currentPlannedFeature)) {
                    solution.unschedule(currentPlannedFeature);
                }
			}
			
			planning.put(employee, employeeNewSchedule);
        }
        
        double endHour = 0.0;
        for (NewSchedule NewSchedule : planning.values())
            for (PlannedFeature pf : NewSchedule.getPlannedFeatures())
                endHour = Math.max(endHour, pf.getEndHour());

		solution.setEmployeesPlanning(planning);
		solution.setEndDate(endHour);

		/* Objectives and quality evaluation */
		SolutionEvaluator evaluator = SolutionEvaluator.getInstance();

		solutionQuality.setAttribute(solution, evaluator.newQuality(solution));
	}

	private void computeHours(PlanningSolution solution) {
        for (PlannedFeature pf : solution.getPlannedFeatures())
            computeHours(solution, pf);
    }

	private void computeHours(PlanningSolution solution, PlannedFeature pf) {
		double newBeginHour = pf.getBeginHour();
		Feature feature = pf.getFeature();
				
		for (Feature previousFeature : feature.getPreviousFeatures()) {
			PlannedFeature previousPlannedFeature = solution.findPlannedFeature(previousFeature);
			if (previousPlannedFeature != null) {
				newBeginHour = Math.max(newBeginHour, previousPlannedFeature.getEndHour());
			}
		}
		//FIXME Check how new algorithm affects this
		//if (prevSolution != null && !prevSolution.getPlannedFeatures().contains(pf)) newBeginHour = Math.max(newBeginHour, replanHour);
		
		pf.setBeginHour(newBeginHour);
	}

	@Override
	public void evaluateConstraints(PlanningSolution solution) {
		int precedencesViolated = 0;
		int violatedConstraints;
		double overall;

		// Precedence constraint
		for (PlannedFeature currentFeature : solution.getPlannedFeatures()) {

			//FIXME check how this affects
			/*// Ignore precedence constraint if the planned feature is frozen in the previous plan
			if (	prevSolution != null &&
					prevSolution.findPlannedFeature(currentFeature.getFeature()) != null &&
					prevSolution.getPlannedFeatures().contains(currentFeature))
			{
			    continue;
            }*/

			for (Feature previousFeature : currentFeature.getFeature().getPreviousFeatures()) {
				PlannedFeature previousPlannedFeature = solution.findPlannedFeature(previousFeature);
				if (previousPlannedFeature == null || previousPlannedFeature.getEndHour() > currentFeature.getBeginHour()) {
					precedencesViolated++;
                }
			}
		}

		overall = -1.0 * precedencesViolated * precedenceConstraintOverall;
		violatedConstraints = precedencesViolated;

		// Check if the solution end date exceeds the deadline
		if (solution.getEndDate() > nbWeeks * nbHoursByWeek) {
			violatedConstraints++;
			overall -= 1.0;
		}

		// Check if the employees assigned to the planned features have the required skills
		for (PlannedFeature plannedFeature : solution.getPlannedFeatures()) {
			List<Skill> featureSkills = plannedFeature.getFeature().getRequiredSkills();
			List<Skill> employeeSkills = plannedFeature.getEmployee().getSkills();
			for (Skill featureSkill : featureSkills) {
				boolean hasSkill = false;
				for (Skill employeeSkill : employeeSkills) {
					if (featureSkill.equals(employeeSkill)) {
						hasSkill = true;
						break;
					}
				}
				if (!hasSkill) {
					violatedConstraints++;
					overall -= 1.0;
				}
			}
		}

		/*if (prevSolution != null) {
			Map<Feature, Employee> previousFeatures = new HashMap<>();

			// Frozen jobs constraint
			for (PlannedFeature pf : prevSolution.getPlannedFeatures()) {
				previousFeatures.put(pf.getFeature(), pf.getEmployee());

				if (pf.isFrozen() && !solution.getPlannedFeatures().contains(pf)) {
					violatedConstraints++;
					overall -= 1.0;
				}
			}

			// Penalize for every feature that was already planned but was assigned another resource
			for (PlannedFeature pf : solution.getPlannedFeatures()) {
				if (previousFeatures.containsKey(pf.getFeature()) &&
						!previousFeatures.get(pf.getFeature()).equals(pf.getEmployee()))
				{
					overall -= 0.1;
				}
			}
		}*/

		numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
		overallConstraintViolation.setAttribute(solution, overall);
		if (violatedConstraints > 0)
			solutionQuality.setAttribute(solution, 0.0);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Next Release Problem to plan in " + nbWeeks + " weeks with " + nbHoursByWeek + "h per week\n");
		sb.append("List of features:\n");
		for (Feature f : features) sb.append("\t" + f.toString() + "\n");
		sb.append("List of employees:\n");
		for (Employee e: employees) sb.append("\t" + e.toString() + "\n");
		return sb.toString();
	}
}
