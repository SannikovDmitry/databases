package ru.mail.park.DAO;

import ru.mail.park.Controllers.DescResponse;

import java.util.List;

public interface PostDAO {
    void truncateTable();

    int count();

    DescResponse create(String postString);

    DescResponse details(String postId, List<String> related);

    DescResponse list(String forumShortName, String threadId, String since, String limit, String order);

    DescResponse removeOrRestore(String postString, String action);

    DescResponse update(String postString);

    DescResponse vote(String voteString);
}
