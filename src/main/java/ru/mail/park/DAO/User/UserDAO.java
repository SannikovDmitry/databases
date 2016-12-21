package ru.mail.park.DAO.User;

import ru.mail.park.Models.ResponseModel;


public interface UserDAO {
    void truncateTable();

    int count();

    ResponseModel details(String email);

    ResponseModel create(String userString);

    ResponseModel follow(String followString);

    ResponseModel unfollow(String followString);

    @SuppressWarnings("MethodParameterNamingConvention")
    ResponseModel listFollowers(String email, String since_id, String limit, String order);

    @SuppressWarnings("MethodParameterNamingConvention")
    ResponseModel listFollowing(String email, String since_id, String limit, String order);

    ResponseModel listPosts(String email, String since, String limit, String order);

    ResponseModel updateProfile(String userString);
}
