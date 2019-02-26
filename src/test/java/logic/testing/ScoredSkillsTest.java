package logic.testing;

import entities.*;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolutionEvaluator;
import logic.SolverNRP;
import logic.test.RandomThings;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScoredSkillsTest {

    private static RandomThings random;
    private static SolverNRP solver;


    @Test
    public void test() {
        random = new RandomThings();
        solver = new SolverNRP();

//        List<Skill> skills = random.skillList(2);
//        List<Feature> features = random.featureList(20);
//        List<Employee> resources = random.employeeList(5);
//
//        random.mix(features, skills, resources);
        //TODO prepare data structure that allows validation

//        NextReleaseProblem nextReleaseProblem =
//                new NextReleaseProblem(features, resources, new DaySlot(0,2,1,8.0,0, null));
//
//        List<PlanningSolution> planningSolutions = solver.executeNRPs(nextReleaseProblem);
//
//        for (PlanningSolution planningSolution : planningSolutions) {
//            System.out.println(planningSolution.toString());
//        }
        Skill s1 = new Skill("S001");
        Skill s2 = new Skill("S002");
        Feature f1 = new Feature("F001", PriorityLevel.HIGH, 10.0, null, s1);
        //Feature f2 = new Feature("F002", PriorityLevel.LOW, 10.0, null, s2);
        Skill es1 = new Skill("S001", 0.5);
        Skill es2 = new Skill("S001", 0.5);
        Skill es21 = new Skill("S002", 0.5);
        Skill es22 = new Skill("S002", 0.5);

        Employee e1 = new Employee("E001", Arrays.asList(es1, es21), random.getAgenda(4,8.0));
        Employee e2 = new Employee("E002", Arrays.asList(es2, es22), random.getAgenda(4,8.0));

        for (int i = 0; i < 100; ++i) {
            NextReleaseProblem nextReleaseProblem =
                    new NextReleaseProblem(Arrays.asList(f1), Arrays.asList(e1,e2));
            List<PlanningSolution> planningSolutions = solver.executeNRPs(nextReleaseProblem);

            for (PlanningSolution ps : planningSolutions) {
                System.out.println(ps.toString());
                Double score = SolutionEvaluator.getInstance().newQuality(ps);
                System.out.println("score: " + score);
                System.out.println("************************");
            }
        }

    }

}
