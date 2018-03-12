package logic.testing;

import java.util.List;

import entities.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.model.ApiPlanningSolution;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolverNRP;
import logic.test.RandomThings;
import logic.test.Validator;

public class ReplanValidationTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ReplanValidationTest.class);
	
	private static SolverNRP solver;
	private static RandomThings random;
    private static Validator validator;
    
    @BeforeClass
    public static void setUpBeforeClass() {
		logger.info("Set up...");
        solver = new SolverNRP(SolverNRP.AlgorithmType.NSGAII);
        random = new RandomThings();
        validator = new Validator();
        logger.info("NRP solver initialized with " + solver.getAlgorithmType() + " algorithm type");
    }

    @Test
    public void validateAll() {

        List<Feature> features = random.featureList(20);
        List<Employee> resources = random.employeeList(4);
        List<Skill> skills = random.skillList(5);

        random.mix(features, skills, resources);

        NextReleaseProblem nrp = new NextReleaseProblem(features, resources);
        PlanningSolution ps = solver.executeNRP(nrp);

        NextReleaseProblem replan = new NextReleaseProblem(features, ps.getEmployees(),
                new DaySlot(0, 2, 1, 8.0, 8.0, null));
        PlanningSolution replanPs = solver.executeNRP(replan);

        validator.validateAll(replanPs);
    }
	
}
