package logic.testing;

import entities.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScheduleTest {

    @Test
    public void testIsFeatureFitMethod() {
        Employee e1 = new Employee("E001", null);
        Employee e2 = new Employee("E002", null);
        Employee e3 = new Employee("E003", null);

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
        agenda.put(e3, Arrays.asList(day8, day2, day1, day4, day9, day6, day7, day3, day5, day10));
        Schedule s = new Schedule(agenda);

        System.out.println(s.toString());

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
        PlannedFeature pf4 = new PlannedFeature(f4, e3);
        s.scheduleFeature(pf4, Arrays.asList(pf1, pf2, pf3));

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

}
