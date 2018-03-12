 package io.swagger.api;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.model.ApiNextReleaseProblem;
import io.swagger.model.ApiPlanningSolution;
import logic.NextReleaseProblem;
import logic.PlanningSolution;
import logic.SolverNRP;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;


 @javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-01T15:48:29.618Z")

@Controller
public class ReplanApiController implements ReplanApi {

    private static Gson gson = ReplanGson.getGson();


    public ResponseEntity<String> replan(HttpServletRequest request) {

        // Deserialize
        String content = requestContentAsString(request);
        if (content == null)
            return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);

        try {
            ApiNextReleaseProblem p = gson.fromJson(content, ApiNextReleaseProblem.class);

            // Convert to internal model
            NextReleaseProblem problem =
                    new NextReleaseProblem(p.getFeatures(), p.getResources(), p.getReplanTime());
            problem.setAlgorithmParameters(problem.getAlgorithmParameters());
            problem.setEvaluationParameters(problem.getEvaluationParameters());

            // Execute
            SolverNRP solver = new SolverNRP();
            PlanningSolution solution = solver.executeNRP(problem);

            // Get solution
            ApiPlanningSolution apiSolution = new ApiPlanningSolution(solution);

            return new ResponseEntity<String>(gson.toJson(apiSolution), HttpStatus.OK);
        }
        catch (JsonSyntaxException e) {
            return new ResponseEntity<String>("Invalid JSON", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<String>(
                    "Something went bad. There's a slight chance of the problem being that you passed some really " +
                            "unexpected data. If that's not the case, it might actually be out fault.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> replanMultipleSolutions(HttpServletRequest request) {
        // Deserialize
        String content = requestContentAsString(request);
        if (content == null)
            return new ResponseEntity<String>("", HttpStatus.BAD_REQUEST);

        try {
            ApiNextReleaseProblem p = gson.fromJson(content, ApiNextReleaseProblem.class);

            // Convert to internal model
            NextReleaseProblem problem =
                    new NextReleaseProblem(p.getFeatures(), p.getResources(), p.getReplanTime());
            problem.setAlgorithmParameters(problem.getAlgorithmParameters());
            problem.setEvaluationParameters(problem.getEvaluationParameters());

            // Execute
            SolverNRP solver = new SolverNRP();
            List<PlanningSolution> solution = solver.executeNRPs(problem);

            // Get solutions
            List<ApiPlanningSolution> planningSolutions = new ArrayList<>();
            for (PlanningSolution ps : solution) planningSolutions.add(new ApiPlanningSolution(ps));

            return new ResponseEntity<String>(gson.toJson(planningSolutions), HttpStatus.OK);
        }
        catch (JsonSyntaxException e) {
            return new ResponseEntity<String>("Invalid JSON", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<String>(
                    "Something went bad. There's a slight chance of the problem being that you passed some really " +
                            "unexpected data. If that's not the case, it might actually be out fault.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String requestContentAsString(HttpServletRequest request) {
        StringBuffer buffer = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                buffer.append(line);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return buffer.toString();
    }

}
