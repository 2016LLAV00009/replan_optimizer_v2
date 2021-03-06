package entities.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EvaluationParameters {
	
	public static final int endDateQuality = 0;
	public static final int completionQuality = 1;
	public static final int distributionQuality = 2;
	public static final int priorityQuality = 3;
	public static final int similarityQuality = 4;
	
	/**
	 * An ordered list with the set of objective weights grouped by priority (key) 
	 */
	private List<HashMap<Integer, Double>> objectivesList;
	
	public EvaluationParameters() {
		objectivesList = new ArrayList<>();
		configureDefaultHighPriorityObjectives();
		configureDefaultLowPriorityObjectives();
		configureDefaultVeryLowPriorityObjectives();
	}
	
	public EvaluationParameters(List<HashMap<Integer, Double>> objectivesList) {
		this.objectivesList = objectivesList;
	}

	private void configureDefaultVeryLowPriorityObjectives() {
		HashMap<Integer, Double> veryLowPriority = new HashMap<>();
		veryLowPriority.put(similarityQuality, 1.0);
		objectivesList.add(veryLowPriority);
	}

	private void configureDefaultLowPriorityObjectives() {
		HashMap<Integer, Double> lowPriority = new HashMap<>();
		lowPriority.put(endDateQuality, (double)1/3);
		lowPriority.put(distributionQuality, (double)1/3);
		lowPriority.put(completionQuality, (double)1/3);
		objectivesList.add(lowPriority);
	}

	private void configureDefaultHighPriorityObjectives() {
		HashMap<Integer, Double> highPriority = new HashMap<>();
		highPriority.put(priorityQuality, 1.0);
		objectivesList.add(highPriority);
	}
	
	public HashMap<Integer, Double> getObjectivesOfPriority(int priority) {
		return objectivesList.get(priority);
	}
	
	public int getPriorityLevels() {
		return objectivesList.size();
	}

}
