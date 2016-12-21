package ru.mail.park.Controllers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.mail.park.DAO.ForumDAO;
import ru.mail.park.DAO.PostDAO;
import ru.mail.park.DAO.ThreadDAO;
import ru.mail.park.DAO.UserDAO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("OverlyBroadThrowsClause")
@RestController
public class GeneralController {

    private final ForumDAO forumDAO;
    private final PostDAO postDAO;
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;

    private static final Logger LOGGER = LogManager.getLogger("COMMON");
    @Autowired
    public GeneralController(UserDAO userDAO, ForumDAO forumDAO, ThreadDAO threadDAO, PostDAO postDAO) {
        this.userDAO = userDAO;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    @RequestMapping(path="/sannikov_httperf_scenario", method = RequestMethod.GET)
    public void getScenario(HttpServletResponse response) {
        final Path file = Paths.get("sannikov_httperf_scenario");

        LOGGER.info("Отдаем сценарий.");
        response.setContentType("txt/plain");
        response.addHeader("Content-Disposition", "attachment; filename=sannikov_httperf_scenario");
        try {
            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @RequestMapping(path = "/db/api/clear", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public DescResponse clear() throws IOException {
        userDAO.truncateTable();
        forumDAO.truncateTable();
        threadDAO.truncateTable();
        postDAO.truncateTable();

        LOGGER.info("clear");
        return new DescResponse("OK", DescResponse.OK);
    }

    @RequestMapping(path = "/db/api/status", method = RequestMethod.GET)
    public DescResponse status() throws IOException {
        final Map<String, Integer> responseBody = new HashMap<>();
        responseBody.put("user", userDAO.count());
        responseBody.put("thread", threadDAO.count());
        responseBody.put("forum", forumDAO.count());
        responseBody.put("post", postDAO.count());

        LOGGER.info("status");
        return new DescResponse(responseBody, DescResponse.OK);
    }
}