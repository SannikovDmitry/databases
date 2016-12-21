package ru.mail.park.DAO.Thread;

import ru.mail.park.Models.ResponseModel;

import java.util.List;

public interface ThreadDAO {

    ResponseModel create(String threadString);

    ResponseModel details(String threadId, List<String> related);

    ResponseModel open(String threadString);

    ResponseModel close(String threadString);

    ResponseModel list(String forumShortName, String email, String since, String limit, String order);

    ResponseModel remove(String threadString);

    ResponseModel restore(String threadString);

    ResponseModel subscribe(String subscribeString);

    ResponseModel unsubscribe(String unsubscribeString);

    ResponseModel update(String threadString);

    ResponseModel vote(String voteString);

    ResponseModel listPosts(String threadId, String sort, String since, String limit, String order);

    void truncateTable();

    int count();

}
