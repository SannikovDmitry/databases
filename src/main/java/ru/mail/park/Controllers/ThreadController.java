package ru.mail.park.Controllers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.DAO.ThreadDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ThreadController {
    private final ThreadDAO threadDAO;
    private static final Logger LOGGER = LogManager.getLogger("THREAD");

    @Autowired
    public ThreadController(ThreadDAO threadDAO) {
        this.threadDAO = threadDAO;
    }


    @RequestMapping(path = "/db/api/thread/create", method = RequestMethod.POST)
    public DescResponse create(@RequestBody String threadString) throws IOException {
        LOGGER.info("create");
        return threadDAO.create(threadString);
    }

    @RequestMapping(path = "/db/api/thread/details", method = RequestMethod.GET)
    public DescResponse details(@RequestParam("thread") String threadId,
                                @RequestParam(value = "related", required = false) List<String> related) throws IOException {
        if(related == null) {
            related = new ArrayList<>();
        }
        LOGGER.info("details");

        return threadDAO.details(threadId, related);
    }

    @RequestMapping(path = "/db/api/thread/close", method = RequestMethod.POST)
    public DescResponse close(@RequestBody String threadString) throws IOException {
        LOGGER.info("close");
        return threadDAO.close(threadString);
    }

    @RequestMapping(path = "/db/api/thread/open", method = RequestMethod.POST)
    public DescResponse open(@RequestBody String threadString) throws IOException {
        LOGGER.info("open");
        return threadDAO.open(threadString);
    }

    @RequestMapping(path = "/db/api/thread/list", method = RequestMethod.GET)
    public DescResponse list(@RequestParam(value = "forum", required = false) String forumShortName,
                             @RequestParam(value = "user", required = false) String email,
                             @RequestParam(value = "since",required = false) String since,
                             @RequestParam(value = "limit",required = false) String limit,
                             @RequestParam(value = "order", required = false) String order) throws IOException {
        LOGGER.info("list");

        return threadDAO.list(forumShortName, email, since, limit, order);
    }

    @RequestMapping(path = "/db/api/thread/remove", method = RequestMethod.POST)
    public DescResponse remove(@RequestBody String threadString) throws IOException {
        LOGGER.info("remove");
        return threadDAO.remove(threadString);
    }

    @RequestMapping(path = "/db/api/thread/restore", method = RequestMethod.POST)
    public DescResponse restore(@RequestBody String threadString) throws IOException {
        LOGGER.info("restore");
        return threadDAO.restore(threadString);
    }

    @RequestMapping(path = "/db/api/thread/subscribe", method = RequestMethod.POST)
    public DescResponse subscribe(@RequestBody String subscribeString) throws IOException {
        LOGGER.info("subscribe");

        return threadDAO.subscribe(subscribeString);
    }

    @RequestMapping(path = "/db/api/thread/unsubscribe", method = RequestMethod.POST)
    public DescResponse unsubscribe(@RequestBody String unsubscribeString) throws IOException {
        LOGGER.info("unsubscribe");

        return threadDAO.unsubscribe(unsubscribeString);
    }

    @RequestMapping(path = "/db/api/thread/update", method = RequestMethod.POST)
    public DescResponse update(@RequestBody String threadString) throws IOException {
        LOGGER.info("update");
        return threadDAO.update(threadString);
    }

    @RequestMapping(path = "/db/api/thread/vote", method = RequestMethod.POST)
    public DescResponse vote(@RequestBody String voteString) throws IOException {
        LOGGER.info("vote");

        return threadDAO.vote(voteString);
    }

    @RequestMapping(path = "/db/api/thread/listPosts", method = RequestMethod.GET)
    public DescResponse listPosts(@RequestParam("thread") String threadId,
                                  @RequestParam(value = "sort", required = false) String sort,
                                  @RequestParam(value = "since",required = false) String since,
                                  @RequestParam(value = "limit",required = false) String limit,
                                  @RequestParam(value = "order",required = false) String order) throws IOException {
        LOGGER.info("listPosts");

        return threadDAO.listPosts(threadId, sort, since, limit, order);
    }
}
