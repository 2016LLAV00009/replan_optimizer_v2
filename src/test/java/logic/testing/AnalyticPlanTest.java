package logic.testing;

import entities.DaySlot;
import entities.Employee;
import entities.Feature;
import entities.Skill;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.PopulationFilter;
import logic.SolverNRP;
import logic.test.RandomThings;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AnalyticPlanTest {

    private static RandomThings random;
    private static SolverNRP solver;


    @Test
    public void test() {

        random = new RandomThings();
        solver = new SolverNRP();

        List<Skill> skills = random.skillList(5);
        List<Feature> features = random.featureList(20);
        List<Employee> resources = random.employeeList(5);

        random.mix(features, skills, resources);

        NextReleaseProblem nextReleaseProblem =
                new NextReleaseProblem(features, resources, new DaySlot(0,2,1,8.0,0, null));

        System.out.println(nextReleaseProblem);
        List<PlanningSolution> planningSolutions = solver.executeNRPs(nextReleaseProblem);

        printSolutions(planningSolutions);

        PlanningSolution ps = PopulationFilter.getBestSolution(planningSolutions);
        DaySlot daySlot = new DaySlot();
        daySlot.setWeek(2);
        daySlot.setDayOfWeek(1);
        daySlot.setBeginHour(8.0);

        System.out.println("************");
        System.out.println("Best solution");
        System.out.println(ps.toString());

        System.out.println("************");
        System.out.println("*Replanning*");
        System.out.println("************");

        NextReleaseProblem replan =
                new NextReleaseProblem(features, ps.getEmployees(), daySlot);
        planningSolutions = solver.executeNRPs(replan);
        printSolutions(planningSolutions);

    }

    private void printSolutions(List<PlanningSolution> planningSolutions) {
        System.out.println(planningSolutions.size() + " solutions");
        for (int i = 0; i < planningSolutions.size(); ++i) {
            for (int j = i+1; j < planningSolutions.size(); ++j) {
                Assert.assertFalse(
                        planningSolutions.get(i).getObjective(0) >
                                planningSolutions.get(j).getObjective(0) &&
                                planningSolutions.get(i).getObjective(1) >
                                        planningSolutions.get(j).getObjective(1) &&
                                planningSolutions.get(i).getObjective(2) >
                                        planningSolutions.get(j).getObjective(2) ||
                                planningSolutions.get(j).getObjective(0) >
                                        planningSolutions.get(i).getObjective(0) &&
                                        planningSolutions.get(j).getObjective(1) >
                                                planningSolutions.get(i).getObjective(1) &&
                                        planningSolutions.get(j).getObjective(2) >
                                                planningSolutions.get(i).getObjective(2));
            }
            System.out.println("Objective 1: " + planningSolutions.get(i).getObjective(0));
            System.out.println("Objective 2: " + planningSolutions.get(i).getObjective(1));
            System.out.println("Objective 3: " + planningSolutions.get(i).getObjective(2));
            System.out.println(planningSolutions.get(i));
        }
    }

}
