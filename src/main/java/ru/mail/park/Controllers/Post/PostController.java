package ru.mail.park.Controllers.Post;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.DAO.Post.PostDAO;
import ru.mail.park.Models.ResponseModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("OverlyBroadThrowsClause")
@RestController
public class PostController {
    private final PostDAO postDAO;
    private static final Logger LOGGER = LogManager.getLogger("POST");

    @Autowired
    public PostController(PostDAO postDAO) {
        this.postDAO = postDAO;
    }


    @RequestMapping(path = "/db/api/post/create", method = RequestMethod.POST)
    public ResponseModel create(@RequestBody String body) throws IOException {
        LOGGER.info("create");
        return postDAO.create(body);
    }

    @RequestMapping(path = "/db/api/post/details", method = RequestMethod.GET)
    public ResponseModel details(@RequestParam("post") String postId,
                                 @RequestParam(value = "related", required = false) List<String> related, @RequestHeader("user-agent") String header) {
        if(related == null) {
            related = new ArrayList<>();
        }
        LOGGER.info("details");
        return postDAO.details(postId, related);
    }

    @RequestMapping(path = "/db/api/post/list", method = RequestMethod.GET)
    public ResponseModel list(@RequestParam(value = "forum", required = false) String forumShortName,
                              @RequestParam(value = "thread", required = false) String threadId,
                              @RequestParam(value = "since", required = false) String since,
                              @RequestParam(value = "limit", required = false) String limit,
                              @RequestParam(value = "order", required = false) String order) {
        LOGGER.info("list");
        return postDAO.list(forumShortName, threadId, since, limit, order);
    }

    @RequestMapping(path = "/db/api/post/remove", method = RequestMethod.POST)
    public ResponseModel remove(@RequestBody String postString) throws IOException {
        LOGGER.info("remove");
        return postDAO.removeOrRestore(postString, "remove");
    }

    @RequestMapping(path = "/db/api/post/restore", method = RequestMethod.POST)
    public ResponseModel restore(@RequestBody String postString) throws IOException {
        LOGGER.info("restore");
        return postDAO.removeOrRestore(postString, "restore");
    }

    @RequestMapping(path = "/db/api/post/vote", method = RequestMethod.POST)
    public ResponseModel vote(@RequestBody String voteString) throws IOException {
        LOGGER.info("vote");
        return postDAO.vote(voteString);
    }

    @RequestMapping(path = "/db/api/post/update", method = RequestMethod.POST)
    public ResponseModel update(@RequestBody String postString) throws IOException {
        LOGGER.info("update");
        return postDAO.update(postString);
    }

}