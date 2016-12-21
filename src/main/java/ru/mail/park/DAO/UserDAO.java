package ru.mail.park.DAO;

import ru.mail.park.Controllers.DescResponse;


public interface UserDAO {
    void truncateTable();

    int count();

    DescResponse details(String email);

    DescResponse create(String userString);

    DescResponse follow(String followString);

    DescResponse unfollow(String followString);

    @SuppressWarnings("MethodParameterNamingConvention")
    DescResponse listFollowers(String email, String since_id, String limit, String order);

    @SuppressWarnings("MethodParameterNamingConvention")
    DescResponse listFollowing(String email, String since_id, String limit, String order);

    DescResponse listPosts(String email, String since, String limit, String order);

    DescResponse updateProfile(String userString);
}
