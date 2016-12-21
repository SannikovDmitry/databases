package ru.mail.park.DAO;

import ru.mail.park.Controllers.DescResponse;

import java.util.List;

public interface ThreadDAO {

    DescResponse create(String threadString);

    DescResponse details(String threadId, List<String> related);

    DescResponse open(String threadString);

    DescResponse close(String threadString);

    DescResponse list(String forumShortName, String email, String since, String limit, String order);

    DescResponse remove(String threadString);

    DescResponse restore(String threadString);

    DescResponse subscribe(String subscribeString);

    DescResponse unsubscribe(String unsubscribeString);

    DescResponse update(String threadString);

    DescResponse vote(String voteString);

    DescResponse listPosts(String threadId, String sort, String since, String limit, String order);

    void truncateTable();

    int count();

}
