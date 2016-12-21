package ru.mail.park.DAOImplentations.Forum;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;
import ru.mail.park.Application;
import ru.mail.park.DAOImplentations.Thread.ThreadDAOimpl;
import ru.mail.park.DAOImplentations.User.UserDAOimpl;
import ru.mail.park.Models.ResponseModel;
import ru.mail.park.DAO.Forum.ForumDAO;
import ru.mail.park.Models.Forum.ForumMainModel;
import ru.mail.park.Models.Post.PostMainModel;
import ru.mail.park.Models.Thread.ThreadMainModel;
import ru.mail.park.Models.User.UserMainModel;

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
        try (Connection connection = Application.connection.getConnection()) {
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
        try (Connection connection = Application.connection.getConnection()) {
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
    public ResponseModel create(String forumString) {
        try {
            final JsonNode json = mapper.readValue(forumString, JsonNode.class);
            try {
                final ForumMainModel forum = new ForumMainModel(
                        json.get("name").getTextValue(),
                        json.get("short_name").getTextValue(),
                        json.get("user").getTextValue() );
                try (Connection connection = Application.connection.getConnection()) {
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

                    return new ResponseModel(forum, ResponseModel.OK);
                } catch (SQLException e) {
                    //noinspection MagicNumber
                    if (e.getErrorCode() == 1062) {
                        return details(json.get("short_name").getTextValue(), new ArrayList<String>());
                    } else {
                        return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
                    }
                }
            } catch (Exception e) {
                return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseModel("INVALID REQUEST", ResponseModel.INVALID_REQUEST);
        }
    }
    @Override
    public ResponseModel details(String forumShortName, final List<String> related) {
        if (forumShortName == null || (!related.isEmpty() && !related.get(0).equals("user"))) {
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }

        try (Connection connection = Application.connection.getConnection()) {
            final String query = "SELECT * FROM forum WHERE short_name=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, forumShortName);
            final ResultSet resultSet = stmt.executeQuery();
            final ForumMainModel forum;
            if (resultSet.next()) {
                forum = new ForumMainModel(resultSet);
            } else {
                return new ResponseModel("NOT FOUND", ResponseModel.NOT_FOUND);
            }
            if (related.contains("user")) {
                forum.setUser(new UserDAOimpl().details((String) forum.getUser()).getResponse());
            }
            stmt.close();

            return new ResponseModel(forum, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel listPosts(String forumShortName,
                                   final List<String> related,
                                   String since,
                                   String limit,
                                   String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("thread") && !str.equals("user"))
                return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
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

            final List<PostMainModel> posts = new ArrayList<>();
            final ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                final PostMainModel post = new PostMainModel(resultSet);

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

            return new ResponseModel(posts, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel listThreads(String forumShortName, final List<String> related, String since,
                                     String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        for (String str : related) {
            if (!str.equals("forum") && !str.equals("user"))
                return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
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
            final List<ThreadMainModel> threads = new ArrayList<>();
            while (resultSet.next()) {
                final ThreadMainModel thread = new ThreadMainModel(resultSet);
                threads.add(thread);

                if (related.contains("forum"))
                    thread.setForum(details(forumShortName, new ArrayList<String>()).getResponse());
                if (related.contains("user")) {
                    final String user = (String) thread.getUser();
                    thread.setUser(new UserDAOimpl().details(user).getResponse());
                }
            }
            stmt.close();

            return new ResponseModel(threads, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public ResponseModel listUsers(String forumShortName, String since_id, String limit, String order) {
        if (forumShortName == null || (order != null && !order.equals("asc") && !order.equals("desc")))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
            final List<UserMainModel> users = new ArrayList<>();

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
                final UserMainModel user = new UserMainModel(resultSet);
                new UserDAOimpl().setFollowers(connection, user);
                new UserDAOimpl().setFollowing(connection, user);
                new UserDAOimpl().setSubscriptions(connection, user);
                if (since_id == null || user.getId() >= new Integer(since_id))
                    users.add(user);
            }

            stmt.close();
            return new ResponseModel(users, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }
}
