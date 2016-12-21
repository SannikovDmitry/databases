package ru.mail.park.DAO.Post;

import ru.mail.park.Models.ResponseModel;

import java.util.List;

public interface PostDAO {
    void truncateTable();

    int count();

    ResponseModel create(String postString);

    ResponseModel details(String postId, List<String> related);

    ResponseModel list(String forumShortName, String threadId, String since, String limit, String order);

    ResponseModel removeOrRestore(String postString, String action);

    ResponseModel update(String postString);

    ResponseModel vote(String voteString);
}
