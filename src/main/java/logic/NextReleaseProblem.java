/**
 * @author Vavou
 */
package logic;

import entities.*;
import entities.parameters.AlgorithmParameters;
import entities.parameters.EvaluationParameters;
import io.swagger.model.ApiNextReleaseProblem;
import io.swagger.model.ApiPlanningSolution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.ConstrainedProblem;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    public static final int INDEX_PRIORITY_OBJECTIVE = 0; // The index of the priority score objective in the objectives list
	public static final int INDEX_SUBOBJECTIVES = 1; // The index of the end date objective in the objectives list
	public static final int INDEX_SIMILARITY_OBJECTIVE = 2;	// Make sure to
	
	private static final Logger logger = LoggerFactory.getLogger(NextReleaseProblem.class);

	// PROBLEM
	private List<Feature> features;
	private List<Employee> employees;
	private DaySlot replanTime;
	private AlgorithmParameters algorithmParameters;
	private EvaluationParameters evaluationParameters;

	//PREVIOUS SOLUTION
	private Schedule previousSchedule;

	// SOLUTION
	private NumberOfViolatedConstraints<PlanningSolution> numberOfViolatedConstraints;
	private OverallConstraintViolation<PlanningSolution> overallConstraintViolation;
	private SolutionQuality solutionQuality;
	private double precedenceConstraintOverall; //The overall of a violated constraint
	private double worstScore; //The priority score if there is no planned feature

    // GETTERS / SETTERS
	public List<Feature> getFeatures() {
		return features;
	}
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	public NumberOfViolatedConstraints<PlanningSolution> getNumberOfViolatedConstraints() {
		return numberOfViolatedConstraints;
	}
	public List<Employee> getSkilledEmployees(List<Skill> reqSkills) {
		ArrayList<Employee> skilledEmployees = new ArrayList<>();
		for (Employee employee : employees)
			//if(employee.getSkills().containsAll(reqSkills))
				skilledEmployees.add(employee);
		return skilledEmployees;
	}
	public List<Employee> getEmployees() {
		return employees;
	}
	public double getWorstScore() {
		return worstScore;
	}

	public SolutionQuality getSolutionQuality() { return solutionQuality; }

	public AlgorithmParameters getAlgorithmParameters() { return algorithmParameters; }
	public void setAlgorithmParameters(AlgorithmParameters algorithmParameters) { this.algorithmParameters = algorithmParameters; }

	public EvaluationParameters getEvaluationParameters() { return evaluationParameters; }
	public void setEvaluationParameters(EvaluationParameters evaluationParameters) { this.evaluationParameters = evaluationParameters;}
	
	// Constructor (empty)
	public NextReleaseProblem() {
		setName("Next Release Problem");
		setNumberOfVariables(1);
		setNumberOfObjectives(3);
		features = new ArrayList<>();
		numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
		overallConstraintViolation = new OverallConstraintViolation<>();
		solutionQuality = new SolutionQuality();
		algorithmParameters = new AlgorithmParameters(SolverNRP.AlgorithmType.NSGAII);
		evaluationParameters = new EvaluationParameters();
	}

	// Constructor
	public NextReleaseProblem(List<Feature> features,
							  List<Employee> employees) {
		this();
		this.employees = employees;
		this.features = new ArrayList<>();

		for (Feature feature : features)
			if (getSkilledEmployees(feature.getRequiredSkills()).size() > 0) // 1.
				if (features.containsAll(feature.getPreviousFeatures())) // 2.
					this.features.add(feature);

		initializeWorstScore();
		initializeNumberOfConstraint();
		this.replanTime = new DaySlot(0,0,0,0,0,null);
		initOldAndNewAgenda();

	}

	public NextReleaseProblem(List<Feature> features, List<Employee> employees, DaySlot replanTime) {
		this(features, employees);
		if (replanTime != null) this.replanTime = replanTime;
		initOldAndNewAgenda();
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	Schedule schedule;

	public HashMap<PlannedFeature, List<DaySlot>> getListPfs() {
		return listPfs;
	}

	public void setListPfs(HashMap<PlannedFeature, List<DaySlot>> listPfs) {
		this.listPfs = listPfs;
	}

	HashMap<PlannedFeature, List<DaySlot>> listPfs = new HashMap<>();


	public void initOldAndNewAgenda() {
		HashMap<Employee, List<DaySlot>> agenda = new HashMap<>();
		HashMap<Employee, List<DaySlot>> previous = new HashMap<>();
		for (Employee e : employees) {
			previous.put(e, e.copyCalendar());
			agenda.put(e, e.copyCalendar());
		}

		schedule = new Schedule(agenda);
		previousSchedule = new Schedule(previous);

		if (replanTime != null) {

			HashMap<Employee, List<DaySlot>> newDaySlots = new HashMap<>();

			//System.out.println(schedule.toString());
			for (Employee e : schedule.getEmployeesCalendar().keySet()) {
				//System.out.println("Let's check employee " + e);
				List<SlotList> slotLists = schedule.getEmployeesCalendar().get(e);
				for (SlotList slotList : slotLists) {
					if (slotList.getSlotStatus().equals(SlotStatus.Used) || slotList.getSlotStatus().equals(SlotStatus.Frozen)) {
						DaySlot beginSlot = slotList.getDaySlot(slotList.getBeginSlotId());
						//If the slotList ends after the replan time, release it
						//System.out.println(schedule.toString());
						if ((getReplanTime().compareByDay(beginSlot) < 0
								|| getReplanTime().compareByDay(beginSlot) == 0 && getReplanTime().getBeginHour() <= beginSlot.getBeginHour())
								&& slotList.getSlotStatus().equals(SlotStatus.Used)) {
							for (DaySlot daySlot : slotList.getDaySlots().values()) {
								daySlot.setStatus(SlotStatus.Free);
								daySlot.setFeatureId(null);
							}
						}
						//If the slotList ends before the replan time, schedule feature
						else {
							Feature f = features.stream().filter(ft -> ft.getName().equals(beginSlot.getFeature())).findFirst().get();
							listPfs.put(new PlannedFeature(f, e), new ArrayList(slotList.getDaySlots().values()));
						}
					}
				}
				List<DaySlot> daySlots = new ArrayList<>();
				for (SlotList slotList : slotLists) daySlots.addAll(slotList.getDaySlots().values());
				newDaySlots.put(e, daySlots);
			}

			Schedule nSchedule = new Schedule(newDaySlots);
			schedule = nSchedule;
			List<String> pfs = new ArrayList<>();
			for (PlannedFeature pf : listPfs.keySet()) pfs.add(pf.getFeature().getName());
			schedule.setPlannedFeatures(pfs);

			setPreviousSchedule(new Schedule(previous));
		}

	}


	// Copy constructor
	public NextReleaseProblem(NextReleaseProblem origin) {
		this(origin.getFeatures(), origin.getEmployees());
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

	public Schedule getPreviousSchedule() {
		return previousSchedule;
	}

	public void setPreviousSchedule(Schedule schedule) { this.previousSchedule = schedule;}

	@Override
	public void evaluate(PlanningSolution solution) {

		replanTime.setEndHour(replanTime.getBeginHour());
		solution.getSchedule().setReplan(replanTime);

		List<PlannedFeature> plannedFeatures = solution.getPlannedFeatures();
		for (int i = 0; i < plannedFeatures.size(); ++i) {
			PlannedFeature currentPlannedFeature = plannedFeatures.get(i);
			List<PlannedFeature> previousPlannedFeatures =
					getPlannedFeatures(solution, currentPlannedFeature.getFeature().getPreviousFeatures());
			if (previousPlannedFeatures != null)
			if (previousPlannedFeatures == null ||
					!solution.getSchedule().scheduleFeature(currentPlannedFeature, previousPlannedFeatures)) {
				solution.unschedule(currentPlannedFeature);
				--i;
			}
		}

		List<Employee> solutionEmployees = new ArrayList<>();
		for (Employee e: employees) {
			List<DaySlot>  daySlots = new ArrayList<>();
			for (SlotList slotList : solution.getSchedule().getEmployeesCalendar().get(e)) {
				daySlots.addAll(slotList.getDaySlots().values());
			}
			solutionEmployees.add(new Employee(e.getName(), new ArrayList<>(e.getSkills()), daySlots));
		}
		solution.setEmployees(solutionEmployees);

		/* Objectives and quality evaluation */
		SolutionEvaluator evaluator = SolutionEvaluator.getInstance();

		solution.setObjective(INDEX_PRIORITY_OBJECTIVE, evaluator.getObjectivePerPriorityLevel(solution, 0));
        solution.setObjective(INDEX_SUBOBJECTIVES, evaluator.getObjectivePerPriorityLevel(solution,1));
        solution.setObjective(INDEX_SIMILARITY_OBJECTIVE, evaluator.similarityObjective(solution));

		solutionQuality.setAttribute(solution, evaluator.newQuality(solution));

	}

	private List<PlannedFeature> getPlannedFeatures(PlanningSolution solution, List<Feature> previousFeatures) {
		List<PlannedFeature> previousPlannedFeatures = new ArrayList<>();
		List<PlannedFeature> plannedFeatures = solution.getPlannedFeatures();
		for (Feature f : previousFeatures) {
			for (PlannedFeature pf : plannedFeatures) {
				if (pf.getFeature().equals(f)) {
					if (pf.getSlotIds() == null || pf.getSlotIds().size() == 0) return null;
					else previousPlannedFeatures.add(pf);
				}
			}
		}
		return previousPlannedFeatures;
	}

	@Override
	public void evaluateConstraints(PlanningSolution solution) {
		int precedencesViolated = 0;
		int violatedConstraints;
		double overall;

		// Precedence constraint
		for (PlannedFeature currentFeature : solution.getPlannedFeatures()) {

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
		//FIXME alternate check
		/*if (solution.getEndDate() > nbWeeks * nbHoursByWeek) {
			violatedConstraints++;
			overall -= 1.0;
		}*/

		// Check if the employees assigned to the planned features have the required skills
//		for (PlannedFeature plannedFeature : solution.getPlannedFeatures()) {
//			List<Skill> featureSkills = plannedFeature.getFeature().getRequiredSkills();
//			List<Skill> employeeSkills = plannedFeature.getEmployee().getSkills();
//			for (Skill featureSkill : featureSkills) {
//				boolean hasSkill = false;
//				for (Skill employeeSkill : employeeSkills) {
//					if (featureSkill.equals(employeeSkill)) {
//						hasSkill = true;
//						break;
//					}
//				}
//				if (!hasSkill) {
//					violatedConstraints++;
//					overall -= 1.0;
//				}
//			}
//		}

		//FIXME alternate check
		/*if (prevSolution != null) {
			Map<Feature, Employee> previousFeatures = new HashMap<>();

			// Frozen jobs constraint
			for (PlannedFeature pf : prevSolution.getJobs()) {
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
		sb.append("Next Release Problem to plan\n");
		sb.append("List of features:\n");
		for (Feature f : features) sb.append("\t" + f.toString() + "\n");
		sb.append("List of employees:\n");
		for (Employee e: employees) sb.append("\t" + e.toString() + "\n");
		return sb.toString();
	}

	public DaySlot getReplanTime() {
		return replanTime;
	}

	public void setReplanTime(DaySlot replanTime) {
		this.replanTime = replanTime;
	}
}
