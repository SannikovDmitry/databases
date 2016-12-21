package ru.mail.park.DAOImplentations;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;
import ru.mail.park.Main;
import ru.mail.park.Controllers.DescResponse;
import ru.mail.park.DAO.PostDAO;
import ru.mail.park.Models.PostDataSet;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"OverlyComplexBooleanExpression", "JDBCResourceOpenedButNotSafelyClosed", "OverlyBroadCatchBlock", "OverlyComplexMethod"})
@Repository
public class PostDAOimpl implements PostDAO {

    final ObjectMapper mapper;

    public PostDAOimpl() {
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Main.connection.getConnection()) {
            final Statement connectStatement = connection.createStatement();
            connectStatement.execute("SET FOREIGN_KEY_CHECKS = 0;");
            connectStatement.execute("TRUNCATE TABLE post;");
            connectStatement.execute("SET FOREIGN_KEY_CHECKS = 1;");
            connectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int count() {
        try (Connection connection = Main.connection.getConnection()) {
            int count = 0;
            final Statement connectionStatement = connection.createStatement();
            final ResultSet result = connectionStatement.executeQuery("SELECT COUNT(*) FROM post");
            while (result.next()) {
                count += result.getInt(1);
            }
            connectionStatement.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    @SuppressWarnings("MagicNumber")
    public DescResponse create(String postString) {
        final PostDataSet dataSet;

        try {
            final JsonNode jsonNode = mapper.readValue(postString, JsonNode.class);
            if (!jsonNode.has("date") || !jsonNode.has("thread") || !jsonNode.has("message")
                    || !jsonNode.has("user") || !jsonNode.has("forum")) {
                return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
            }
            try {
                dataSet = new PostDataSet(jsonNode);
            } catch (Exception e) {
                return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new DescResponse("INVALID REQUEST", DescResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES(?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, dataSet.getDate());
            preparedStatement.setInt(2, (Integer) dataSet.getThread());
            preparedStatement.setString(3, dataSet.getMessage());
            preparedStatement.setString(4, (String) dataSet.getUser());
            preparedStatement.setString(5, (String) dataSet.getForum());
            preparedStatement.setObject(6, dataSet.getParent());
            preparedStatement.setBoolean(7, dataSet.getIsApproved());
            preparedStatement.setBoolean(8, dataSet.getIsHighlighted());
            preparedStatement.setBoolean(9, dataSet.getIsEdited());
            preparedStatement.setBoolean(10, dataSet.getIsSpam());
            preparedStatement.setBoolean(11, dataSet.getIsDeleted());
            preparedStatement.execute();

            final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                dataSet.setId(generatedKeys.getInt(1));
            }
            preparedStatement.close();

            final String queryCountUserForum = "SELECT COUNT(*) FROM user_forum where user=? AND forum=?;";
            final PreparedStatement preparedStatement1 = connection.prepareStatement(queryCountUserForum);
            preparedStatement1.setString(1, (String) dataSet.getUser());
            preparedStatement1.setString(2, (String) dataSet.getForum());

            final ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            final int countUserForum = rs.getInt(1);
            preparedStatement1.close();
            if (countUserForum == 0) {
                final String queryInsertUserForum = "INSERT INTO user_forum (user, forum) VALUES(?,?)";
                final PreparedStatement preparedStatement2 = connection.prepareStatement(queryInsertUserForum);
                preparedStatement2.setString(1, (String) dataSet.getUser());
                preparedStatement2.setString(2, (String) dataSet.getForum());
                preparedStatement2.execute();
                preparedStatement2.close();
            }

            final byte code = (byte) dataSet.getId();
            if (dataSet.getParent() == null || dataSet.getParent() == 0) {
                final String querySetPaths = "UPDATE post SET first_path=? WHERE id=?";
                preparedStatement = connection.prepareStatement(querySetPaths);
                preparedStatement.setInt(1, dataSet.getId());
                preparedStatement.setInt(2, dataSet.getId());
                preparedStatement.execute();
                preparedStatement.close();
            } else {
                final String queryGetParent = "SELECT first_path,last_path FROM post WHERE id=?";
                preparedStatement = connection.prepareStatement(queryGetParent);
                preparedStatement.setObject(1, dataSet.getParent());
                final ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                final int parentFirstPath = resultSet.getInt("first_path");
                final String parentLastPath = resultSet.getString("last_path");
                preparedStatement.close();

                final String querySetPaths = "UPDATE post SET first_path=?, last_path=? WHERE id=?";
                preparedStatement = connection.prepareStatement(querySetPaths);
                preparedStatement.setInt(1, parentFirstPath);
                if (parentLastPath != null) {
                    preparedStatement.setString(2, parentLastPath + '.' + (char) code);
                } else {
                    preparedStatement.setObject(2, (char) code, Types.CHAR);
                }
                preparedStatement.setObject(3, dataSet.getId());
                preparedStatement.execute();
                preparedStatement.close();
            }

            if (!dataSet.getIsDeleted()) {
                final String queryThreadPosts = "UPDATE thread SET posts=posts+1 WHERE id=?";
                preparedStatement = connection.prepareStatement(queryThreadPosts);
                preparedStatement.setInt(1, (Integer) dataSet.getThread());
                preparedStatement.execute();
                preparedStatement.close();
            }

            return new DescResponse(dataSet, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public DescResponse details(String postId, final List<String> related) {
        if (postId == null)
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);

        if (related != null) {
            for (String str : related) {
                if (!str.equals("user") && !str.equals("forum") && !str.equals("thread")) {
                    return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
                }
            }
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String query = "SELECT * FROM post WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, postId);
            final ResultSet resultSet = stmt.executeQuery();
            final PostDataSet post;

            if (resultSet.next()) {
                post = new PostDataSet(resultSet);
            } else {
                return new DescResponse("NOT FOUND", DescResponse.NOT_FOUND);
            }
            if (related.contains("user")) {
                post.setUser(new UserDAOimpl().details((String) post.getUser()).getResponse());
            }
            if (related.contains("thread")) {
                post.setThread(new ThreadDAOimpl().details(post.getThread().toString(), new ArrayList<String>()).getResponse());
            }
            if (related.contains("forum")) {
                post.setForum(new ForumDAOimpl().details((String) post.getForum(), new ArrayList<String>()).getResponse());
            }

            stmt.close();

            return new DescResponse(post, DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public DescResponse list(String forumShortName, String threadId, String since,
                             String limit, String order) {
        if ((forumShortName == null && threadId == null) || (forumShortName != null && threadId != null)
                || (order != null && !order.equals("asc") && !order.equals("desc"))) {
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Main.connection.getConnection()) {
            String forumQuery = "SELECT * FROM forum WHERE short_name=?;";
            String threadQuery = "SELECT * FROM thread WHERE id=?";
            final String existQuery = (forumShortName != null) ? forumQuery : threadQuery;
            final PreparedStatement existStmt = connection.prepareStatement(existQuery);
            existStmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            final ResultSet existResultSet = existStmt.executeQuery();

            if (!existResultSet.next()) {
                existStmt.close();
                return new DescResponse("NOT FOUND", DescResponse.NOT_FOUND);
            }
            existStmt.close();

            forumQuery = "SELECT * FROM post WHERE forum=?";
            threadQuery = "SELECT * FROM post WHERE thread=?";
            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append((forumShortName != null) ? forumQuery : threadQuery);
            if (since != null) queryBuilder.append(" AND date >=?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, (forumShortName != null) ? forumShortName : threadId);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));

            final ResultSet resultSet = stmt.executeQuery();
            final List<PostDataSet> posts = new ArrayList<>();
            while (resultSet.next()) {
                final PostDataSet post = new PostDataSet(resultSet);
                posts.add(post);
            }
            stmt.close();

            return new DescResponse(posts, DescResponse.OK);

        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);

        }
    }

    @Override
    public DescResponse removeOrRestore(String postString, String action) {
        final JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            return new DescResponse("INVALID REQUEST", DescResponse.INVALID_REQUEST);
        }

        try (Connection connection = Main.connection.getConnection()) {
            final String queryPost = "SELECT thread, isDeleted FROM post WHERE id=?";
            PreparedStatement stmt = connection.prepareStatement(queryPost);
            stmt.setInt(1, json.get("post").getIntValue());
            final ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                final int threadId = resultSet.getInt("thread");
                final boolean oldIsDeleted = resultSet.getBoolean("isDeleted");
                stmt.close();
                final String queryRemove = "UPDATE post SET isDeleted=? WHERE id=?";
                stmt = connection.prepareStatement(queryRemove);
                stmt.setInt(1, (action.equals("remove")) ? 1 : 0);
                stmt.setInt(2, json.get("post").getIntValue());
                stmt.executeUpdate();

                if (!oldIsDeleted && action.equals("remove") || oldIsDeleted && action.equals("restore")) {
                    stmt.close();
                    final String queryThreadPostsDec = "UPDATE thread SET posts=posts-1 WHERE id=?";
                    final String queryThreadPostsInc = "UPDATE thread SET posts=posts+1 WHERE id=?";
                    final String queryThreadPosts = (action.equals("remove") ? queryThreadPostsDec : queryThreadPostsInc);
                    stmt = connection.prepareStatement(queryThreadPosts);
                    stmt.setInt(1, threadId);
                    stmt.executeUpdate();
                }
            }
            stmt.close();

            return new DescResponse(json.toString(), DescResponse.OK);
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);

        }
    }

    @Override
    public DescResponse update(String postString) {
        final JsonNode json;
        try {
            json = mapper.readValue(postString, JsonNode.class);
        } catch (IOException e) {
            return new DescResponse("INVALID REQUEST", DescResponse.INVALID_REQUEST);
        }

        if (!json.has("post") || !json.has("message"))
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);

        final String message = json.get("message").getTextValue();
        final int postId = json.get("post").getIntValue();
        try (Connection connection = Main.connection.getConnection()) {
            final String query = "UPDATE post SET message=? WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, message);
            stmt.setInt(2, postId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(postId), new ArrayList<String>());
        } catch (SQLException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

    @Override
    public DescResponse vote(String voteString) {
        final JsonNode json;
        try {
            json = mapper.readValue(voteString, JsonNode.class);
        } catch (IOException e) {
            return new DescResponse("INVALID REQUEST", DescResponse.INVALID_REQUEST);
        }

        if (!json.has("vote") || !json.has("post") ||
                json.get("vote").getIntValue() != 1 && json.get("vote").getIntValue() != -1) {
            return new DescResponse("INCORRECT REQUEST", DescResponse.INCORRECT_REQUEST);
        }
        final int vote = json.get("vote").getIntValue();
        final int postId = json.get("post").getIntValue();

        try (Connection connection = Main.connection.getConnection()) {
            final String queryLike = "UPDATE post SET likes=likes+1, points=points+1 WHERE id=?";
            final String queryDislike = "UPDATE post SET dislikes=dislikes+1, points=points-1 WHERE id=?";
            final PreparedStatement stmt = connection.prepareStatement((vote == 1) ? queryLike : queryDislike);
            stmt.setInt(1, postId);
            stmt.executeUpdate();
            stmt.close();

            return details(Integer.toString(postId), new ArrayList<String>());
        } catch (SQLException | NullPointerException e) {
            return new DescResponse("UNKNOWN ERROR", DescResponse.UNKNOWN_ERROR);
        }
    }

}

