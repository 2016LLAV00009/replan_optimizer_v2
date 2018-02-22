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

        DaySlot day1 = new DaySlot(1,1,1, 8.0, 15.0, SlotStatus.Free);
        DaySlot day2 = new DaySlot(2, 1, 1, 15.0, 17.0, SlotStatus.Used);
        DaySlot day3 = new DaySlot(3, 1, 2, 8.0, 17.0, SlotStatus.Used);
        DaySlot day4 = new DaySlot(4, 1, 3, 8.0, 12.0, SlotStatus.Used);
        DaySlot day5 = new DaySlot(5, 1, 3, 12.0, 17.0, SlotStatus.Free);
        DaySlot day6 = new DaySlot(6, 2, 1, 8.0, 17.0, SlotStatus.Free);
        DaySlot day7 = new DaySlot(7, 2, 2, 8.0, 12.0, SlotStatus.Free);
        DaySlot day8 = new DaySlot( 8, 2, 2, 12.0, 15.0, SlotStatus.Used);
        DaySlot day9 = new DaySlot(9, 2, 2, 15.0, 17.0, SlotStatus.Free);

        DaySlot day11 = new DaySlot(10,1,1, 8.0, 15.0, SlotStatus.Free);
        DaySlot day12 = new DaySlot(11, 1, 1, 15.0, 17.0, SlotStatus.Used);
        DaySlot day13 = new DaySlot(12, 1, 2, 8.0, 17.0, SlotStatus.Used);
        DaySlot day14 = new DaySlot(13, 1, 3, 8.0, 12.0, SlotStatus.Used);
        DaySlot day15 = new DaySlot(14, 1, 3, 12.0, 17.0, SlotStatus.Free);
        DaySlot day16 = new DaySlot(15, 2, 1, 8.0, 17.0, SlotStatus.Free);
        DaySlot day17 = new DaySlot(16, 2, 2, 8.0, 12.0, SlotStatus.Free);
        DaySlot day18 = new DaySlot( 17, 2, 2, 12.0, 15.0, SlotStatus.Used);
        DaySlot day19 = new DaySlot(18, 2, 2, 15.0, 17.0, SlotStatus.Free);

        HashMap<Employee, List<DaySlot>> agenda = new HashMap<>();
        agenda.put(e1, Arrays.asList(day8, day2, day1, day4, day9, day6, day7, day3, day5));
        agenda.put(e2, Arrays.asList(day18, day12, day11, day14, day19, day16, day17, day13, day15));
        Schedule s = new Schedule(agenda);

        System.out.println(s.toString());

    }

}
