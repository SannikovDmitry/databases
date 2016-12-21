package ru.mail.park.DAO;

import ru.mail.park.Controllers.DescResponse;

import java.util.List;


public interface ForumDAO {
    void truncateTable();

    int count();

    DescResponse create(String body);

    DescResponse details(String forumShortName, final List<String> related);

    DescResponse listPosts(String forumShortName,
                           final List<String> related,
                           String since,
                           String limit,
                           String order);

    DescResponse listThreads(String forumShortName,
                             final List<String> related,
                             String since,
                             String limit,
                             String order);

    @SuppressWarnings("MethodParameterNamingConvention")
    DescResponse listUsers(String forumShortName, String since_id, String limit, String order);
}
