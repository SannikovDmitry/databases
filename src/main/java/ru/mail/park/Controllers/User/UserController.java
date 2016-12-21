package ru.mail.park.Controllers.User;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.mail.park.DAO.User.UserDAO;
import ru.mail.park.Models.ResponseModel;


@RestController
public class UserController {
    private final UserDAO userDAO;
    private static final Logger LOGGER = LogManager.getLogger("USER");

    @Autowired
    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/db/api/user/details", method = RequestMethod.GET)
    public ResponseModel details(@RequestParam("user") String email) {
        LOGGER.info("details");

        return userDAO.details(email);
    }

    @RequestMapping(path = "/db/api/user/create", method = RequestMethod.POST)
    public ResponseModel create(@RequestBody String userString) {
        return userDAO.create(userString);
    }

    @RequestMapping(path = "/db/api/user/follow", method = RequestMethod.POST)
    public ResponseModel follow(@RequestBody String followString) {
        LOGGER.info("create");

        return userDAO.follow(followString);
    }

    @RequestMapping(path = "/db/api/user/unfollow", method = RequestMethod.POST)
    public ResponseModel unfollow(@RequestBody String unfollowString) {

        LOGGER.info("unfollow");
        return userDAO.unfollow(unfollowString);
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @RequestMapping(path = "/db/api/user/listFollowers", method = RequestMethod.GET)
    public ResponseModel listFollowers(@RequestParam("user") String email,
                                       @RequestParam(value = "since_id",required = false) String since_id,
                                       @RequestParam(value = "order",required = false) String order,
                                       @RequestParam(value = "limit",required = false) String limit) {
        LOGGER.info("listFollowers");

        return userDAO.listFollowers(email, since_id, limit, order);
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @RequestMapping(path = "/db/api/user/listFollowing", method = RequestMethod.GET)
    public ResponseModel listFollowing(@RequestParam("user") String email,
                                       @RequestParam(value = "since_id",required = false) String since_id,
                                       @RequestParam(value = "order",required = false) String order,
                                       @RequestParam(value = "limit",required = false) String limit) {
        LOGGER.info("listFollowing");

        return userDAO.listFollowing(email, since_id, limit, order);
    }

    @RequestMapping(path = "/db/api/user/listPosts", method = RequestMethod.GET)
    public ResponseModel listPosts(@RequestParam("user") String email,
                                   @RequestParam(value = "since", required = false) String since,
                                   @RequestParam(value = "order",required = false) String order,
                                   @RequestParam(value = "limit",required = false) String limit) {
        LOGGER.info("listPost");
        return userDAO.listPosts(email, since, limit, order);
    }

    @RequestMapping(path = "/db/api/user/updateProfile", method = RequestMethod.POST)
    public ResponseModel updateProfile(@RequestBody String userString) {
        LOGGER.info("updateProfile");

        return userDAO.updateProfile(userString);
    }
}