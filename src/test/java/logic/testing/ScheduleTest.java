package logic.testing;

import entities.*;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolutionEvaluator;
import logic.SolverNRP;
import logic.test.RandomThings;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScheduleTest {

    RandomThings randomThings = new RandomThings();

    //@Test
    public void testIsFeatureFitMethod() {
        Employee e1 = new Employee("E001", null);
        Employee e2 = new Employee("E002", null);

        DaySlot day1 = new DaySlot(1,1,1, 8.0, 17.0, SlotStatus.Free);
        DaySlot day2 = new DaySlot(2, 1, 2, 8.0, 17.0, SlotStatus.Free);
        DaySlot day3 = new DaySlot(3, 1, 3, 8.0, 17.0, SlotStatus.Free);
        DaySlot day4 = new DaySlot(4, 1, 4, 8.0, 17.0, SlotStatus.Free);
        DaySlot day5 = new DaySlot(5, 1, 5, 8.0, 17.0, SlotStatus.Free);
        DaySlot day6 = new DaySlot(6, 2, 1, 8.0, 17.0, SlotStatus.Free);
        DaySlot day7 = new DaySlot(7, 2, 2, 8.0, 17.0, SlotStatus.Free);
        DaySlot day8 = new DaySlot(8, 2, 3, 8.0, 17.0, SlotStatus.Free);
        DaySlot day9 = new DaySlot(9, 2, 4, 8.0, 17.0, SlotStatus.Free);
        DaySlot day10 = new DaySlot(10, 2, 5, 8.0, 17.0, SlotStatus.Free);

        HashMap<Employee, List<DaySlot>> agenda = new HashMap<>();
        agenda.put(e1, Arrays.asList(day8, day2, day1, day4, day9, day6, day7, day3, day5, day10));
        agenda.put(e2, Arrays.asList(day8, day2, day1, day4, day9, day6, day7, day3, day5, day10));
        Schedule s = new Schedule(agenda);

        //System.out.println(s.toString());

        Feature f1 = new Feature("F001",PriorityLevel.HIGH, 15.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf1 = new PlannedFeature(f1, e1);
        s.scheduleFeature(pf1, null);

        Feature f2 = new Feature("F002",PriorityLevel.HIGH, 25.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf2 = new PlannedFeature(f2, e2);
        s.scheduleFeature(pf2, null);

        Feature f3 = new Feature("F003",PriorityLevel.HIGH, 25.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf3 = new PlannedFeature(f3, e1);
        s.scheduleFeature(pf3, Arrays.asList(pf2));

        Feature f4 = new Feature("F004",PriorityLevel.HIGH, 40.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf4 = new PlannedFeature(f4, e2);
        s.scheduleFeature(pf4, Arrays.asList(pf1, pf2, pf3));

        Feature f5 = new Feature("F005",PriorityLevel.HIGH, 10.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf5 = new PlannedFeature(f5, e1);
        s.scheduleFeature(pf5, null);

        Feature f6 = new Feature("F006",PriorityLevel.HIGH, 25.0, new ArrayList<>(), new ArrayList<>());
        PlannedFeature pf6 = new PlannedFeature(f6, e2);
        s.scheduleFeature(pf6, null);

        System.out.println("*****AFTER SCHEDULING F001*****\n");
        System.out.println(s.toString());

        /*Feature f2 = new Feature("F002",PriorityLevel.HIGH, 7.0, new ArrayList<>(), new ArrayList<>());
        s.scheduleFeature(new PlannedFeature(f2, e1), null);

        System.out.println("*****AFTER SCHEDULING F001*****\n");
        System.out.println(s.toString());

        Feature f3 = new Feature("F002",PriorityLevel.HIGH, 2.0, new ArrayList<>(), new ArrayList<>());
        s.scheduleFeature(new PlannedFeature(f3, e1), null);

        System.out.println("*****AFTER SCHEDULING F003*****\n");
        System.out.println(s.toString());*/

    }

    @Test
    public void test() {
        List<Employee> employees = randomThings.employeeList(1);
        List<Feature> features = randomThings.featureList(5);

        DaySlot day1 = new DaySlot(1,1,1, 8.0, 17.0, SlotStatus.Used);
        day1.setFeature(features.get(0));
        DaySlot day2 = new DaySlot(2, 1, 2, 8.0, 17.0, SlotStatus.Used);
        day2.setFeature(features.get(0));
        DaySlot day3 = new DaySlot(3, 1, 3, 8.0, 17.0, SlotStatus.Used);
        day3.setFeature(features.get(1));
        DaySlot day4 = new DaySlot(4, 1, 4, 8.0, 17.0, SlotStatus.Free);
        DaySlot day5 = new DaySlot(5, 1, 5, 8.0, 17.0, SlotStatus.Used);

        DaySlot day6 = new DaySlot(6, 2, 1, 8.0, 17.0, SlotStatus.Free);
        DaySlot day7 = new DaySlot(7, 2, 2, 8.0, 17.0, SlotStatus.Free);
        DaySlot day8 = new DaySlot(8, 2, 3, 8.0, 17.0, SlotStatus.Free);
        DaySlot day9 = new DaySlot(9, 2, 4, 8.0, 17.0, SlotStatus.Free);
        DaySlot day10 = new DaySlot(10, 2, 5, 8.0, 17.0, SlotStatus.Free);


        day5.setFeature(features.get(2));

        employees.get(0).setCalendar(Arrays.asList(day1,day2,day3,day4,day5,day6,day7,day8,day9,day10));

        DaySlot replan = new DaySlot(0, 1, 3, 8, 0, null);
        NextReleaseProblem next = new NextReleaseProblem(features, employees, replan);
        PlanningSolution ps = new PlanningSolution(next);

        System.out.println(ps.getSchedule());
        SolverNRP solver = new SolverNRP();
        PlanningSolution solution = solver.executeNRP(next);
        System.out.println(solution.toString());


    }

    @Test
    @Ignore
    public void testPlan() {
        List<Employee> employees = randomThings.employeeList(4);
        List<Feature> features = randomThings.featureList(20);

        features.get(3).getPreviousFeatures().add(features.get(0));
        features.get(4).getPreviousFeatures().add(features.get(3));
        features.get(10).getPreviousFeatures().add(features.get(9));
        features.get(11).getPreviousFeatures().add(features.get(10));
        features.get(12).getPreviousFeatures().add(features.get(11));
        features.get(18).getPreviousFeatures().add(features.get(17));

        List<DaySlot> agenda = randomThings.getAgenda(2, 8.0);

        for (Employee e : employees) e.setCalendar(agenda);

        SolverNRP solver = new SolverNRP();
        NextReleaseProblem nrp = new NextReleaseProblem(features, employees);
        System.out.println(nrp.toString());

        PlanningSolution solution = solver.executeNRP(nrp);
        System.out.println(solution);

        System.out.println("*************************");

        Schedule schedule = solution.getSchedule();
        List<Employee> employees1 = solution.getEmployees();

        //for (Employee e : employees1) System.out.println(e.getCalendar());
        //System.out.println(schedule);

        NextReleaseProblem next = new NextReleaseProblem(features, employees1);
        next.setReplanTime(new DaySlot(0, 2, 1, 8.0, 0, null));
        PlanningSolution ps = new PlanningSolution(next);

        System.out.println(ps.getSchedule());



    }

}
