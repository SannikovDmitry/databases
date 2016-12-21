package ru.mail.park.DAO.Forum;

import ru.mail.park.Models.ResponseModel;

import java.util.List;


public interface ForumDAO {
    void truncateTable();

    int count();

    ResponseModel create(String body);

    ResponseModel details(String forumShortName, final List<String> related);

    ResponseModel listPosts(String forumShortName,
                            final List<String> related,
                            String since,
                            String limit,
                            String order);

    ResponseModel listThreads(String forumShortName,
                              final List<String> related,
                              String since,
                              String limit,
                              String order);

    @SuppressWarnings("MethodParameterNamingConvention")
    ResponseModel listUsers(String forumShortName, String since_id, String limit, String order);
}
