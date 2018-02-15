package logic;

import entities.Employee;
import entities.Feature;
import entities.PlannedFeature;
import entities.NewSchedule;
import entities.parameters.AlgorithmParameters;
import entities.parameters.EvaluationParameters;
import logic.analytics.Analytics;
import logic.analytics.EmployeeAnalytics;
import logic.analytics.Utils;
import logic.comparators.PlanningSolutionDominanceComparator;
import logic.operators.PlanningCrossoverOperator;
import logic.operators.PlanningMutationOperator;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.measure.MeasureManager;
import org.uma.jmetal.measure.PullMeasure;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.neighborhood.impl.C9;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The class that
 */
public class SolverNRP {

    public enum AlgorithmType {
        NSGAII("NSGA-II"), MOCell("MOCell"), SPEA2("SPEA2"), PESA2("PESA2"), SMSEMOA("SMSEMOA");

        private String name;

        AlgorithmType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static AlgorithmType fromName(String name) {
            switch (name) {
                case "NSGA-II":
                    return NSGAII;
                case "MOCell":
                    return MOCell;
                case "SPEA2":
                    return SPEA2;
                case "PESA2":
                    return PESA2;
                case "SMSEMOA":
                    return SMSEMOA;
            }
            return null;
        }
    }

    private Algorithm<List<PlanningSolution>> algorithm;
    private AlgorithmType algorithmType;
    private EvaluationParameters evaluationParameters;


    /**
     * The empty construtor defaults the algorithm to NSGA-II
     */
    public SolverNRP() {
        algorithm = null;
        algorithmType = AlgorithmType.NSGAII;
        evaluationParameters = new EvaluationParameters();
    }

    /**
     * Constructor with an arbitrary {@link AlgorithmType}
     */
    public SolverNRP(AlgorithmType algorithmType) {
        this();
        this.algorithmType = algorithmType;
    }


    private Algorithm<List<PlanningSolution>> createAlgorithm(AlgorithmType algorithmType, NextReleaseProblem problem) {
        CrossoverOperator<PlanningSolution> crossover;
        MutationOperator<PlanningSolution> mutation;
        SelectionOperator<List<PlanningSolution>, PlanningSolution> selection;

        crossover = new PlanningCrossoverOperator(problem);
        mutation = new PlanningMutationOperator(problem);
        selection = new BinaryTournamentSelection<>(new PlanningSolutionDominanceComparator());

        AlgorithmParameters parameters = problem.getAlgorithmParameters();
        int nbIterations = parameters.getNumberOfIterations();
        int populationSize = parameters.getPopulationSize();

        switch (algorithmType) {
            case NSGAII:
                return new NSGAIIBuilder<>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(nbIterations)
                        .setPopulationSize(populationSize)
                        .build();
            case MOCell:
                return new MOCellBuilder<>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(nbIterations)
                        .setPopulationSize(populationSize)    // sqrt(populationSize) tiene que ser entero
                        .setNeighborhood(new C9<>((int) Math.sqrt(2500), (int) Math.sqrt(2500)))
                        .build();
            case SPEA2:
                return new SPEA2Builder<>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxIterations(nbIterations)
                        .setPopulationSize(populationSize)
                        .build();
            case PESA2:
                return new PESA2Builder<>(problem, crossover, mutation)
                        .setMaxEvaluations(nbIterations)
                        .setPopulationSize(populationSize)
                        .build();
            case SMSEMOA:
                return new SMSEMOABuilder<>(problem, crossover, mutation)
                        .setSelectionOperator(selection)
                        .setMaxEvaluations(nbIterations)
                        .setPopulationSize(populationSize)
                        .build();
            default:
                return createAlgorithm(AlgorithmType.MOCell, problem);
        }
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }


    /**
     * Executes the algorithm on the given problem
     * @return A valid solution. Note that an empty solution is a valid one too.
     */
    public PlanningSolution executeNRP(NextReleaseProblem problem) {
        if (problem.getAlgorithmParameters() == null)
            problem.setAlgorithmParameters(new AlgorithmParameters(algorithmType));
        else
            algorithmType = problem.getAlgorithmParameters().getAlgorithmType();
        
        if (problem.getEvaluationParameters() == null)
        	problem.setEvaluationParameters(new EvaluationParameters());

        //PlanningSolution solution = this.generatePlanningSolution(problem);
        PlanningSolution solution = this.generatePlanningSolutionWithMeasures(problem);
        
        solution.setAnalytics(new Analytics(solution));

        /*postprocess(solution);

        clearSolutionIfNotValid(solution);

        //FIXME added re-evaluation
        SolutionQuality solutionQuality = new SolutionQuality();
        problem.evaluate(solution);
        
        System.out.println(String.format("%f, %d/%d after postprocessing)", solutionQuality.getAttribute(solution), 
                solution.getPlannedFeatures().size(), problem.getFeatures().size()));*/

        return solution;
    }

    // Tries to schedule undone features to the least busy employee if there is enough time
    /*@Deprecated
    private void postprocess(PlanningSolution solution) {
        Utils utils = new Utils(solution);
        NextReleaseProblem problem = solution.getProblem();
        Map<Employee, EmployeeAnalytics> employeesInfo = new HashMap<>();

        for (Employee e : solution.getProblem().getEmployees())
            employeesInfo.put(e, new EmployeeAnalytics(e, solution));

        for (Feature f : solution.getUndoneFeatures()) {

            // Skip any frozen feature left unplanned because it would likely generate an invalid solution
            if (problem.getPreviousSolution() != null) {
                PlannedFeature pf = problem.getPreviousSolution().findPlannedFeature(f);
                if (pf != null && pf.isFrozen())
                    continue;
            }

            if (utils.allPrecedencesArePlanned(f)) {
                List<Employee> doableBy = utils.doableBy(f).stream()
                        .filter(e -> utils.hadEnoughTime(e, f))
                        .filter(e -> utils.couldRespectPrecedences(e, f))
                        .collect(Collectors.toList());

                if (!doableBy.isEmpty()) {
                    Employee e = doableBy.get(0);

                    // Let's assign it to the least busy employee
                    for (Employee e2 : doableBy)
                        if (employeesInfo.get(e2).getWorkload() < employeesInfo.get(e).getWorkload())
                            e = e2;

                    PlannedFeature pf = new PlannedFeature(f, e);
                    utils.computeHours(pf);
                    NewSchedule s = solution.getEmployeesPlanning().get(e);

                    s.scheduleFeature(pf);

                    // Don't forget to update the solution's internal state
                    solution.getPlannedFeatures().add(pf);
                    solution.getUndoneFeatures().remove(f);
                    //FIXME updated end date when reprocessing
                    solution.setEndDate(Math.max(pf.getEndHour(), solution.getEndDate()));
                }
            }
        }
    }*/

    /*
        The generated solution might violate constraints in case the solver does not find a better one.
        In that case, the planning is invalid and should be cleared.
    */
    private void clearSolutionIfNotValid(PlanningSolution solution) {
        NumberOfViolatedConstraints<PlanningSolution> numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
        // TODO: We should know the reason of the violation.
        if (numberOfViolatedConstraints.getAttribute(solution) > 0) {
            solution.getEmployeesPlanning().clear();
            for (PlannedFeature plannedFeature : solution.getPlannedFeatures())
                solution.unschedule(plannedFeature);
        }
    }

    private PlanningSolution generatePlanningSolution(NextReleaseProblem problem) {

        algorithm = createAlgorithm(algorithmType, problem);
        
        new AlgorithmRunner.Executor(algorithm).execute();

        List<PlanningSolution> result = algorithm.getResult();
        PlanningSolution bestSolution = PopulationFilter.getBestSolutions(result).iterator().next();

        printQuality(result, bestSolution);

        return bestSolution;
    }
    
    private static final int POPULATION_MEASURE = 0;
	private static final int MEASURE = 1;
    
    private PlanningSolution generatePlanningSolutionWithMeasures(NextReleaseProblem problem) {
    	
    	//FIXME Repeated code
    	
    	CrossoverOperator<PlanningSolution> crossover;
        MutationOperator<PlanningSolution> mutation;
        SelectionOperator<List<PlanningSolution>, PlanningSolution> selection;
        SequentialSolutionListEvaluator<PlanningSolution> evaluator;


        crossover = new PlanningCrossoverOperator(problem);
        mutation = new PlanningMutationOperator(problem);
        selection = new BinaryTournamentSelection<>(new PlanningSolutionDominanceComparator());
        evaluator = new SequentialSolutionListEvaluator<>();

        AlgorithmParameters parameters = problem.getAlgorithmParameters();
        int nbIterations = parameters.getNumberOfIterations();
        int populationSize = parameters.getPopulationSize();
    	
    	CustomAlgorithm algorithm = new CustomAlgorithm(problem, nbIterations, populationSize, crossover, mutation, selection, evaluator);
    	PullMeasure<List<PlanningSolution>> populationMeasure = algorithm.getMeasureManager().getPullMeasure(POPULATION_MEASURE);
    	PullMeasure<Integer> iterationMeasure = algorithm.getMeasureManager().getPullMeasure(MEASURE);
    	
    	Thread thread = new Thread(algorithm);
    	thread.start();
    	
    	SolutionQuality quality = new SolutionQuality();
    	StringBuilder sb = new StringBuilder();
    	int lastIt = -1;
    	while (thread.isAlive()) {
    		List<PlanningSolution> newPopulation = populationMeasure.get();
    		int currentIt = iterationMeasure.get();
    		if (newPopulation != null && currentIt != lastIt) {
    			lastIt = currentIt;
    			try {
    				PlanningSolution ps = PopulationFilter.getBestSolution(newPopulation);
        			if (ps != null && quality.getAttribute(ps) != null) {
            			sb.append("Current population " + newPopulation.size() + " (iteration " + iterationMeasure.get() + "):\n");
        				sb.append("Best solution " + ps.getPlannedFeatures().size() + " features with " + quality.getAttribute(ps) + "\n");
        			}
    			} catch (Exception e) {
    				
    			}
    		}
    	}
    	try {
			PrintWriter out = new PrintWriter("/home/jmotger/Escritorio/experiments/data.txt");
			out.println(sb.toString());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}    	
    	
    	List<PlanningSolution> result = algorithm.getResult();
        PlanningSolution bestSolution = PopulationFilter.getBestSolutions(result).iterator().next();

        printQuality(result, bestSolution);

        return bestSolution;
        
    }

	private void printQuality(List<PlanningSolution> solutions, PlanningSolution best) {
        SolutionQuality solutionQuality = new SolutionQuality();
        double totalQuality = 0.0;
        int totalPlannedFeatures = 0;
        for (PlanningSolution solution : solutions) {
            totalQuality += solutionQuality.getAttribute(solution);
            totalPlannedFeatures += solution.getPlannedFeatures().size();
        }

        double averageQuality = totalQuality/solutions.size();
        int averagePlannedFeatures = totalPlannedFeatures/solutions.size();

        int nbFeatures = solutions.get(0).getProblem().getFeatures().size();

        String message = "Average solution quality: %f. Average planned features: %d/%d (best solution: %f, %d/%d)\n";
        System.out.print(
                String.format(Locale.ENGLISH, message,
                        averageQuality, averagePlannedFeatures, nbFeatures,
                        solutionQuality.getAttribute(best), best.getPlannedFeatures().size(), nbFeatures));
    }
}
