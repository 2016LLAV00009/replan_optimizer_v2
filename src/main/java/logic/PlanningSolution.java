// @author Vavou
package logic;

import entities.*;
import logic.analytics.Analytics;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A solution for a particular {@link NextReleaseProblem}.
 * It also includes chunks of logic which should probably go somewhere else.
 */
public class PlanningSolution extends AbstractGenericSolution<Integer, NextReleaseProblem> implements Comparable<PlanningSolution> {

	private static final long serialVersionUID = 615615442782301271L; //Generated Id
	
	private static final int INDEX_NB_FEATURES_VARIABLE = 0;
	
    /**
     * If true, the solution will be randomly initialized on creation. There is a particular case in which you won't
     * want this to happen, when creating child solutions in {@link logic.operators.PlanningCrossoverOperator}.
     */
    private boolean INITIALIZE_ON_CREATE = true;
	
    private CopyOnWriteArrayList<PlannedFeature> plannedFeatures; 	            // Included features
	private CopyOnWriteArrayList<Feature> undoneFeatures; 						// Not included features
	private Schedule schedule;
    private Analytics analytics = null;

    private List<Employee> employees;
    
    /* --- GETTERS / SETTERS --- */

	public int size() {
		return plannedFeatures.size();
	}

    public List<PlannedFeature> getPlannedFeatures() {
		return plannedFeatures;
	}
	public PlannedFeature getPlannedFeature(int position) {
		if (position >= 0 && position < plannedFeatures.size())
			return plannedFeatures.get(position);
		return null;
	}

	public Schedule getSchedule() {
		return this.schedule;
	}
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public List<PlannedFeature> getEndPlannedFeaturesSubListCopy(int beginPosition) {
		return new ArrayList<>(plannedFeatures.subList(beginPosition, plannedFeatures.size()));
	}

	public List<Feature> getUndoneFeatures() {
		return undoneFeatures;
	}

    public NextReleaseProblem getProblem() { return problem; }

    public Analytics getAnalytics() { return analytics; }
    public void setAnalytics(Analytics analytics) { this.analytics = analytics; }

	public List<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(List<Employee> employees) {
		this.employees = employees;
	}

	/* --- CONSTRUCTORS --- */

    // constructor (normal)
	public PlanningSolution(NextReleaseProblem problem) {
		super(problem);
		this.problem = problem;
		this.employees = problem.getEmployees();
	    //numberOfViolatedConstraints = 0;
		initializePlannedFeatureVariables();
	    initializeObjectiveValues();
	}

	private void forceSchedule(PlannedFeature pf, List<DaySlot> daySlots) {
		getSchedule().forcedSchedule(pf, daySlots);
		undoneFeatures.remove(pf.getFeature());
		plannedFeatures.add(pf);
	}

	public PlanningSolution(NextReleaseProblem problem, boolean initializeOnCreate) {
        super(problem);
        INITIALIZE_ON_CREATE = initializeOnCreate;
        this.employees = problem.getEmployees();
        //numberOfViolatedConstraints = 0;
        initializePlannedFeatureVariables();
        initializeObjectiveValues();
    }

    // Copy constructor
	public PlanningSolution(PlanningSolution origin) {
		super(origin.problem);

	    //numberOfViolatedConstraints = origin.numberOfViolatedConstraints;

	    plannedFeatures = new CopyOnWriteArrayList<>();
	    for (PlannedFeature plannedFeature : origin.getPlannedFeatures())
			plannedFeatures.add(new PlannedFeature(plannedFeature));
	    
	    // Copy constraints and quality
	    this.attributes.putAll(origin.attributes);
	    this.schedule = origin.schedule;
	    
	    for (int i = 0 ; i < origin.getNumberOfObjectives() ; i++)
	    	this.setObjective(i, origin.getObjective(i));

	    undoneFeatures = new CopyOnWriteArrayList<>(origin.getUndoneFeatures());
	}


	// Exchange the two features in positions pos1 and pos2
	public void exchange(int pos1, int pos2) {
		if (pos1 >= 0 && pos2 >= 0 && pos1 < plannedFeatures.size() && pos2 < plannedFeatures.size() && pos1 != pos2) {
			PlannedFeature feature1 = plannedFeatures.get(pos1);
			plannedFeatures.set(pos1, plannedFeatures.get(pos2));
			plannedFeatures.set(pos2, feature1);
		}
	}
	
    // Calculate the sum of the priority of each feature
	public double getPriorityScore() {
		double score = problem.getWorstScore();
		for (PlannedFeature plannedFeature : plannedFeatures)
			score -= plannedFeature.getFeature().getPriority().getScore();
		return score;
	}
	
	// Returns all of the planned features done by a specific employee
	public List<PlannedFeature> getFeaturesDoneBy(Employee e) {
		List<PlannedFeature> featuresOfEmployee = new ArrayList<>();
		for (PlannedFeature plannedFeature : plannedFeatures)
			if (plannedFeature.getEmployee().equals(e))
				featuresOfEmployee.add(plannedFeature);
		return featuresOfEmployee;
	}

	// Return true if the feature is already in the planned features
	public boolean isAlreadyPlanned(Feature feature) {
		for (PlannedFeature pf : plannedFeatures)
			if (pf.getFeature().equals(feature))
				return true;

		return false;
	}

	// Returns the planned feature corresponding to the feature given in parameter
	public PlannedFeature findPlannedFeature(Feature feature) {
		for (PlannedFeature plannedFeature : plannedFeatures)
			if (plannedFeature.getFeature().equals(feature))
				return plannedFeature;
		return null;
	}
	
	// Initialize the variables. Load a random number of planned features
	private void initializePlannedFeatureVariables() {
		int nbFeaturesToDo = problem.getFeatures().size();
		setVariableValue(INDEX_NB_FEATURES_VARIABLE, nbFeaturesToDo);
		undoneFeatures = new CopyOnWriteArrayList<>();
		undoneFeatures.addAll(problem.getFeatures());
		plannedFeatures = new CopyOnWriteArrayList<>();

		initOldAndNewAgenda();

		if (INITIALIZE_ON_CREATE) {
			//System.out.println(undoneFeatures.size()+ "//" + plannedFeatures.size());
            if (randomGenerator.nextDouble() > getProblem().getAlgorithmParameters().getRateOfNotRandomSolution())
                initializePlannedFeaturesRandomly(nbFeaturesToDo);
            else
                initializePlannedFeaturesWithPrecedences(nbFeaturesToDo);
        }
		
	}

	private void initOldAndNewAgenda() {
		this.schedule = new Schedule(problem.getSchedule());
		for (PlannedFeature pf : problem.getListPfs().keySet()) {
			forceSchedule(pf, problem.getListPfs().get(pf));
		}
	}

	private double computeCriticalPath() {
		double currentCriticalPath = 0.;
		for (PlannedFeature pf : getPlannedFeatures()) {
			double newPath = getPathDuration(pf.getFeature());
			if (newPath > currentCriticalPath) currentCriticalPath = newPath;
		}
		return currentCriticalPath;
	}

	private double getPathDuration(Feature feature) {
		if (feature.getPreviousFeatures().isEmpty())
			return feature.getDuration();
		else {
			double maxPath = 0.;
			for (Feature f : feature.getPreviousFeatures()) {
				double path = getPathDuration(f);
				if (path > maxPath) maxPath = path;
			}
			return maxPath + feature.getDuration();
		}
	}

	// Initializes the planned features randomly
    private void initializePlannedFeaturesRandomly(int numFeaturesToPlan) {
        Feature featureToDo;
        List<Employee> skilledEmployees;

        for (int i = 0 ; i < numFeaturesToPlan ; i++) {
            featureToDo = undoneFeatures.get(randomGenerator.nextInt(0, undoneFeatures.size()-1));
            skilledEmployees = problem.getSkilledEmployees(featureToDo.getRequiredSkills());

            if (skilledEmployees.size() > 0)
                scheduleAtTheEnd(featureToDo,
                        skilledEmployees.get(randomGenerator.nextInt(0, skilledEmployees.size() - 1)));
        }
    }

	// Initializes the planned features considering the precedences
	private void initializePlannedFeaturesWithPrecedences(int numFeaturesToPlan) {
		Feature featureToDo;
		List<Employee> skilledEmployees;
		List<Feature> possibleFeatures = updatePossibleFeatures();
		int i = 0;
		while (i < numFeaturesToPlan && possibleFeatures.size() > 0) {
			featureToDo = possibleFeatures.get(randomGenerator.nextInt(0, possibleFeatures.size()-1));
			skilledEmployees = problem.getSkilledEmployees(featureToDo.getRequiredSkills());
			scheduleAtTheEnd(featureToDo, skilledEmployees.get(randomGenerator.nextInt(0, skilledEmployees.size()-1)));
			possibleFeatures = updatePossibleFeatures();
			i++;
		}
	}

	// schedule a planned feature to a position in the planning
	public void schedule(int position, Feature feature, Employee e) {
		undoneFeatures.remove(feature);
		plannedFeatures.add(position, new PlannedFeature(feature, e));
	}
		
	// schedule a feature in the planning
	public void scheduleAtTheEnd(Feature feature, Employee e) {
		if (isAlreadyPlanned(feature)) plannedFeatures.remove(findPlannedFeature(feature));
		else undoneFeatures.remove(feature);
		PlannedFeature newPlannedFeature = new PlannedFeature(feature, e);
        plannedFeatures.add(newPlannedFeature);
	}
	
	// schedule a random undone feature to a random place in the planning
	public void scheduleRandomFeature() {
		scheduleRandomFeature(randomGenerator.nextInt(0, plannedFeatures.size()));
	}
	
	// schedule a random feature to insertionPosition of the planning list
	public void scheduleRandomFeature(int insertionPosition) {
		if (undoneFeatures.size() <= 0)
			return;
		Feature newFeature = undoneFeatures.get(randomGenerator.nextInt(0, undoneFeatures.size() -1));
		List<Employee> skilledEmployees = problem.getSkilledEmployees(newFeature.getRequiredSkills());
		Employee newEmployee = skilledEmployees.get(randomGenerator.nextInt(0, skilledEmployees.size()-1));
		schedule(insertionPosition, newFeature, newEmployee);
	}

	// schedule a feature : remove it from the planned features and add it to the undone ones
	public void unschedule(PlannedFeature plannedFeature) {
		if (isAlreadyPlanned(plannedFeature.getFeature())) {
			undoneFeatures.add(plannedFeature.getFeature());
			plannedFeatures.remove(plannedFeature);
		}
	}

	// Creates a list of the possible features to do regarding to the precedences of the undone features
	private List<Feature> updatePossibleFeatures() {
		List<Feature> possibleFeatures = new ArrayList<>();
		boolean possible;
		int i;
		
		for (Feature feature : undoneFeatures) {
			possible = true;
			i = 0;
			while (possible && i < feature.getPreviousFeatures().size()) {
				if (!isAlreadyPlanned(feature.getPreviousFeatures().get(i)))
					possible = false;
				i++;
			}
			if (possible)
				possibleFeatures.add(feature);
		}

		return possibleFeatures;
	}

	@Override
	public String getVariableValueString(int index) {
		return getVariableValue(index).toString();	// I guess this is what you want
	}


	@Override
	public Solution<Integer> copy() {
		return new PlanningSolution(this);
	}
	
	@Override
	public int hashCode() {
		return getPlannedFeatures().size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;

		PlanningSolution other = (PlanningSolution) obj;

		if (this.getPlannedFeatures().size() == other.getPlannedFeatures().size())
			return this.getPlannedFeatures().containsAll(other.getPlannedFeatures());

		return false;
	}
	
	@Override
	//TODO better to string
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%d/%d features planned", plannedFeatures.size(), 
				plannedFeatures.size() + undoneFeatures.size()));
		sb.append("\n");
		/*if (employeesPlanning != null) {
			for (Employee e : employeesPlanning.keySet()) {
				sb.append("Employee " + e.getName() + " Schedule\n");
				for (PlannedFeature pf : employeesPlanning.get(e).getPlannedFeatures()) {
					sb.append("\tFeature " + pf.getFeature() + " from " + pf.getBeginHour() + " to " + pf.getEndHour() + " (" + pf.getFeature().getDuration() + "h)\n");
				}
			}
		}*/
		if (schedule != null) {
			for (Employee e : schedule.getEmployeesCalendar().keySet()) {
				sb.append("Employee " + e.getName() + " schedule\n");
				for (SlotList slotList : schedule.getEmployeesCalendar().get(e)) {
					if (slotList.getSlotStatus().equals(SlotStatus.Used)) {
						sb.append(slotList.toString());
					}
				}
			}
		}
		return sb.toString();
	}

	public String toR() {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        List<PlannedFeature> plannedFeatures = getPlannedFeatures();
        List<Employee> resources = getResources();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        final int OneHour = 60 * 60 * 1000;

        sb.append("getDataFromUser <- function(d) {").append(lineSeparator);

        sb.append(lineSeparator);

        for (PlannedFeature pf : plannedFeatures)
        	sb      .append("  d$plan[nrow(d$plan)+1,] <- c(")
                    .append(quote(pf.getFeature().getName())).append(", ") // id
                    .append(quote(pf.getFeature().getName())).append(", ") // content
                    .append(quote(dateFormat.format(new Date(curDate.getTime()+OneHour*(int)pf.getBeginHour())))).append(", ") // start
                    .append(quote(dateFormat.format(new Date(curDate.getTime()+OneHour*(int)pf.getEndHour())))).append(", ") // end
                    .append(quote(pf.getEmployee().getName())).append(", ") // group
                    .append(quote("range")).append(", ") // type
                    .append(pf.getFeature().getPriority().ordinal()+1).append(", ") // priority
                    .append((int)pf.getFeature().getDuration()).append(")").append(lineSeparator); // effort

        sb.append(lineSeparator);

        for (Employee e : resources) {
            sb.append("  d$resources[nrow(d$resources)+1,] <- c(")
                    .append(quote(e.getName())).append(", ") // id
                    .append(quote(e.getName())).append(", "); // content
            for(Skill s : e.getSkills())
                sb.append("  d$skillsGraphEdges[nrow(d$skillsGraphEdges)+1,] <- c(")
                        .append(quote(e.getName())).append(", ")
                        .append(quote(s.getName())).append(")").append(lineSeparator);
        }

		sb.append(lineSeparator);

        for (Feature f : problem.getFeatures()) {
            sb.append("  d$features[nrow(d$features)+1,] <- c(")
                    .append(quote(f.getName())).append(", ") // id
                    .append(quote(f.getName())).append(", ") // id
                    .append(quote(isAlreadyPlanned(f) ? "Yes" : "No")).append(", ") // id
                    .append(f.getPriority().ordinal() + 1).append(", ") // priority
                    .append((int) f.getDuration()).append(")").append(lineSeparator); // effort
            for(Feature d : f.getPreviousFeatures())
                sb.append("  d$depGraphEdges[nrow(d$depGraphEdges)+1,] <- c(")
                        .append(quote(f.getName())).append(", ")
                        .append(quote(d.getName())).append(")").append(lineSeparator);
            for(Skill s : f.getRequiredSkills())
                sb.append("  d$reqSkillsGraphEdges[nrow(d$reqSkillsGraphEdges)+1,] <- c(")
                        .append(quote(f.getName())).append(", ")
                        .append(quote(s.getName())).append(")").append(lineSeparator);

        }

        sb.append(lineSeparator);

        //sb.append("  d$nWeeks <- ").append(problem.getNbWeeks()).append(lineSeparator);

        sb.append(lineSeparator);

        sb.append("  return(d)").append(lineSeparator);
        sb.append("}").append(lineSeparator);

        return sb.toString();
    }

    private String quote(String s) {
	    return "\"".concat(s).concat("\"");
    }

    private List<Employee> getResources() {
	    ArrayList<Employee> employees = new ArrayList<>();
        for (PlannedFeature feature : getPlannedFeatures())
            if(!employees.contains(feature.getEmployee())) employees.add(feature.getEmployee());
        return employees;
    }


    public DaySlot getCriticalEndSlot() {
        double computeCriticalPath = computeCriticalPath();
        return new DaySlot(0, Math.floorDiv((int)computeCriticalPath, (24*7)) + 1, Math.floorDiv((int)computeCriticalPath, 24) + 1,
				computeCriticalPath % 24, computeCriticalPath % 24, null);
    }

	@Override
	public int compareTo(PlanningSolution o) {
		double ps1 = this.getProblem().getSolutionQuality().getAttribute(this);
		double ps2 = o.getProblem().getSolutionQuality().getAttribute(o);
		if (ps1 > ps2 ) return 1;
		else if (ps1 < ps2) return -1;
		else return 0;
	}
}
