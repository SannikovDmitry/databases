package ru.mail.park.Controllers.Forum;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.DAO.Forum.ForumDAO;
import ru.mail.park.Models.ResponseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("OverlyBroadThrowsClause")
@RestController()
public class ForumController {
    private final ForumDAO forumDAO;

    private static final Logger LOGGER = LogManager.getLogger("FORUM");

    @Autowired
    public ForumController(ForumDAO forumDAO) {
        this.forumDAO = forumDAO;
    }


    @RequestMapping(path = "/db/api/forum/create", method = RequestMethod.POST)
    public ResponseModel create(@RequestBody String body) throws IOException {
        LOGGER.info("create");
        return forumDAO.create(body);
    }

    @RequestMapping(path = "/db/api/forum/details", method = RequestMethod.GET)
    public ResponseModel details(@RequestParam("forum") String forumShortName,
                                 @RequestParam(value = "related", required = false) List<String> related) {
        if (related == null) {
            related = new ArrayList<>();
        }
        LOGGER.info("details");
        return forumDAO.details(forumShortName, related);
    }

    @RequestMapping(path = "/db/api/forum/listPosts", method = RequestMethod.GET)
    public ResponseModel listPosts(@RequestParam("forum") String forumShortName,
                                   @RequestParam(value = "related", required = false) List<String> related,
                                   @RequestParam(value = "since", required = false) String since,
                                   @RequestParam(value = "limit", required = false) String limit,
                                   @RequestParam(value = "order", required = false) String order) {
        if (related == null) {
            related = new ArrayList<>();
        }
        LOGGER.info("listPosts");

        return forumDAO.listPosts(forumShortName, related, since, limit, order);
    }

    @RequestMapping(path = "/db/api/forum/listThreads", method = RequestMethod.GET)
    public ResponseModel listThreads(@RequestParam("forum") String forumShortName,
                                     @RequestParam(value = "related", required = false) List<String> related,
                                     @RequestParam(value = "since", required = false) String since,
                                     @RequestParam(value = "limit", required = false) String limit,
                                     @RequestParam(value = "order", required = false) String order) {
        if (related == null) {
            related = new ArrayList<>();
        }
        LOGGER.info("listThread");
        return forumDAO.listThreads(forumShortName, related, since, limit, order);
    }

    @RequestMapping(path = "/db/api/forum/listUsers", method = RequestMethod.GET)
    public ResponseModel listUsers(@RequestParam("forum") String forumShortName,
                                   @RequestParam(value = "since_id", required = false) String since_id,
                                   @RequestParam(value = "limit", required = false) String limit,
                                   @RequestParam(value = "order", required = false) String order) {
        LOGGER.info("listUsers");
        return forumDAO.listUsers(forumShortName, since_id, limit, order);
    }
}
