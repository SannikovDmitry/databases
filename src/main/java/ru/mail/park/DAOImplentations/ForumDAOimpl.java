package ru.mail.park.DAOImplentations;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;
import ru.mail.park.Main;
import ru.mail.park.Controllers.DescResponse;
import ru.mail.park.DAO.ForumDAO;
import ru.mail.park.Models.ForumDataSet;
import ru.mail.park.Models.PostDataSet;
import ru.mail.park.Models.ThreadDataSet;
import ru.mail.park.Models.UserDataSet;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"OverlyComplexBooleanExpression", "OverlyComplexMethod", "JDBCResourceOpenedButNotSafelyClosed", "OverlyBroadCatchBlock"})
@Repository
public class ForumDAOimpl implements ForumDAO {
    private final ObjectMapper mapper;

    public ForumDAOimpl() {
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE forum;");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM forum;");
            resultSet.next();
            final int count = resultSet.getInt(1);
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public DescResponse create(String forumString) {
        try {
            final JsonNode json = mapper.readValue(forumString, JsonNode.class);
            try {
                final ForumDataSet forum = new ForumDataSet(
                        json.get("name").getTextValue(),
                        json.get("short_name").getTextValue(),
                        json.get("user").getTextValue() );
                try (Connection connection = Main.connection.getConnection()) {
                    final String query = "INSERT INTO forum (name, short_name, user) VALUES(?,?,?)";
                    final PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, forum.getName());
                    stmt.setString(2, forum.getShort_name());
                    stmt.setString(3, (String)forum.getUser());
                    stmt.execute();
                    final ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next())
                        forum.setId(generatedKeys.getInt(1));
                    stmt.close();

                    return new DescResponse(forum, DescResponse.OK);
                } catch (SQLException e) {
                    //noinspection MagicNumber
                    if (e.getErrorCode() == 1062) {
                        return details(json.get("short_name").getTextValue(), new ArrayList<String>());
                    } else {
                        return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
                    }
                }
            } catch (Exception e) {
                return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new DescResponse("INVALID REQUEST", DescResponse.INVALID_REQUEST);
        }
    }
    @Override
    public DescResponse details(String forumShortName, final List<String> related) {
        if (forumShortName == null || (!related.isEmpty() && !related.get(0).equals("user"))) {
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "SELECT * FROM forum WHERE short_name=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, forumShortName);
            final ResultSet resultSet = stmt.executeQuery();
            final ForumDataSet forum;
            if (resultSet.next()) {
                forum = new ForumDataSet(resultSet);
            } else {
                return new DescResponse("NOT FOUND", DescResponse.NOT_FOUND);
            }
            if (related.contains("user")) {
                forum.setUser(new UserDAOimpl().details((String) forum.getUser()).getResponse());
            }
            stmt.close();

            return new DescResponse(forum, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public DescResponse listPosts(String forumShortName,
                                  final List<String> related,
                                  String since,
                                  String limit,
                                  String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("thread") && !str.equals("user"))
                return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM post WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");


            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final List<PostDataSet> posts = new ArrayList<>();
            final ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                final PostDataSet post = new PostDataSet(resultSet);

                if (related.contains("forum"))
                    post.setForum(details(forumShortName, new ArrayList<String>()).getResponse());
                if (related.contains("thread")) {
                    final Integer thread = (Integer) post.getThread();
                    post.setThread(new ThreadDAOimpl().details(thread.toString(),
                            new ArrayList<String>()).getResponse());
                }
                if (related.contains("user")) {
                    final String user = (String) post.getUser();
                    post.setUser(new UserDAOimpl().details(user).getResponse());
                }

                posts.add(post);
            }
            stmt.close();

            return new DescResponse(posts, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public DescResponse listThreads(String forumShortName, final List<String> related, String since,
                                    String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("user"))
                return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * FROM thread WHERE forum=?");
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (!order.equals("asc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<ThreadDataSet> threads = new ArrayList<>();
            while (resultSet.next()) {
                final ThreadDataSet thread = new ThreadDataSet(resultSet);
                threads.add(thread);

                if (related.contains("forum"))
                    thread.setForum(details(forumShortName, new ArrayList<String>()).getResponse());
                if (related.contains("user")) {
                    final String user = (String) thread.getUser();
                    thread.setUser(new UserDAOimpl().details(user).getResponse());
                }
            }
            stmt.close();

            return new DescResponse(threads, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public DescResponse listUsers(String forumShortName, String since_id, String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            final List<UserDataSet> users = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT u.* FROM user u");
            queryBuilder.append(" JOIN user_forum uf ON u.email=uf.user");
            queryBuilder.append(" WHERE uf.forum = ?");
            if (since_id != null) queryBuilder.append(" AND u.id >= ?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, forumShortName);
             int param = 2;
            if (since_id != null) stmt.setString(param++, since_id);
            if (limit != null) stmt.setInt(param, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                final UserDataSet user = new UserDataSet(resultSet);
                new UserDAOimpl().setFollowers(connection, user);
                new UserDAOimpl().setFollowing(connection, user);
                new UserDAOimpl().setSubscriptions(connection, user);
                if (since_id == null || user.getId() >= new Integer(since_id))
                    users.add(user);
            }

            stmt.close();
            return new DescResponse(users, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }
}
