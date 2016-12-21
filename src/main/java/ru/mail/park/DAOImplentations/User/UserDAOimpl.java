package ru.mail.park.DAOImplentations.User;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;
import ru.mail.park.Application;
import ru.mail.park.DAOImplentations.Post.PostDAOimpl;
import ru.mail.park.Models.ResponseModel;
import ru.mail.park.DAO.User.UserDAO;
import ru.mail.park.Models.Post.PostMainModel;
import ru.mail.park.Models.User.UserMainModel;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Repository
public class UserDAOimpl implements UserDAO {
    final ObjectMapper mapper;

    public UserDAOimpl() {
        mapper = new ObjectMapper();
        mapper.getJsonFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    @Override
    public void truncateTable() {
        try (Connection connection = Application.connection.getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.execute("TRUNCATE TABLE user;");
            stmt.execute("TRUNCATE TABLE follows;");
            stmt.execute("TRUNCATE TABLE user_forum;");
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
            final ResultSet resultSet = stmt.executeQuery("SELECT COUNT(*) FROM user;");
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
    public ResponseModel details(String email) {
        try (Connection connection = Application.connection.getConnection()) {
            final PreparedStatement stmt = connection.prepareStatement("SELECT * FROM user WHERE email = ?;", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, email);
            final ResultSet resultSet = stmt.executeQuery();
            final UserMainModel user;
            if (resultSet.next()) {
                user = new UserMainModel(resultSet);
                setFollowers(connection, user);
                setFollowing(connection, user);
                setSubscriptions(connection, user);
                stmt.close();
            } else {
                stmt.close();
                return new ResponseModel("NOT FOUND", ResponseModel.NOT_FOUND);
            }

            return new ResponseModel(user, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel create(String userString) {
        final UserMainModel user;

        try {
            final JsonNode json = mapper.readValue(userString, JsonNode.class);
            if (!json.has("email") || !json.has("username") || !json.has("name") || !json.has("about")) {
                return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
            }
            try {
                user = new UserMainModel(json);

            } catch (Exception e) {
                return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseModel("INVALID REQUEST", ResponseModel.INVALID_REQUEST);
        }

        try (Connection connection = Application.connection.getConnection()) {
            final String query = "INSERT INTO user (email, username, name, about, isAnonymous) VALUES(?,?,?,?,?);";
            final PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getAbout());
            stmt.setBoolean(5, user.getIsAnonymous());
            stmt.execute();
            final ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next())
                user.setId(generatedKeys.getInt(1));
            stmt.close();

            return new ResponseModel(user, ResponseModel.OK);
        } catch (SQLException e) {
            //noinspection MagicNumber
            if (e.getErrorCode() == 1062) {
                return new ResponseModel("ALREADY EXISTS", ResponseModel.ALREADY_EXIST);
            } else {
                return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
            }
        }
    }

    @Override
    public ResponseModel follow(String followString) {
        final JsonNode json;
        try {
            json = mapper.readValue(followString, JsonNode.class);
        } catch (IOException e) {
            return new ResponseModel("INVALID REQUEST", ResponseModel.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);

        final String follower = json.get("follower").getTextValue();
        final String followed = json.get("followee").getTextValue();
        try (Connection connection = Application.connection.getConnection()) {
            final String query = "INSERT IGNORE INTO follows (follower, followed) VALUES (?,?)";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.executeUpdate();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel unfollow(String unfollowString) {
        final JsonNode json;
        try {
            json = mapper.readValue(unfollowString, JsonNode.class);
        } catch (IOException e) {
            return new ResponseModel("INVALID REQUEST", ResponseModel.INVALID_REQUEST);
        }

        if (!json.has("follower") || !json.has("followee"))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);

        final String follower = json.get("follower").getTextValue();
        final String followed = json.get("followee").getTextValue();
        try (Connection connection = Application.connection.getConnection()) {
            final String query = "DELETE FROM follows WHERE follower=? AND followed=?;";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, follower);
            stmt.setString(2, followed);
            stmt.execute();
            stmt.close();

            return details(follower);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public ResponseModel listFollowers(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
            final List<UserMainModel> followers = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT follower FROM follows f JOIN user u ON f.follower = u.email");
            queryBuilder.append(" WHERE f.followed=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final UserMainModel follower = (UserMainModel) details(resultSet.getString(1)).getResponse();
                if (since_id == null || follower.getId() >= new Integer(since_id))
                    followers.add(follower);
            }

            stmt.close();
            return new ResponseModel(followers, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @SuppressWarnings("MethodParameterNamingConvention")
    @Override
    public ResponseModel listFollowing(String email, String since_id, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
            final List<UserMainModel> following = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT followed FROM follows f JOIN user u ON f.followed = u.email");
            queryBuilder.append(" WHERE f.follower=?");
            queryBuilder.append(" ORDER BY u.name");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            if (limit != null) stmt.setInt(2, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final UserMainModel followed = (UserMainModel) details(resultSet.getString(1)).getResponse();
                if (since_id == null || followed.getId() >= new Integer(since_id))
                    following.add(followed);
            }

            stmt.close();
            return new ResponseModel(following, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel listPosts(String email, String since, String limit, String order) {
        if (email == null || order != null && !order.equals("asc") && !order.equals("desc")) {
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);
        }
        order = (order == null) ? "desc" : order;

        try (Connection connection = Application.connection.getConnection()) {
            final List<PostMainModel> posts = new ArrayList<>();

            final StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT id FROM post WHERE user = ?");
            if (since != null) queryBuilder.append("AND date >= ?");
            queryBuilder.append(" ORDER BY date");
            if (order.equals("desc")) queryBuilder.append(" DESC");
            if (limit != null) queryBuilder.append(" LIMIT ?");

            final PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            stmt.setString(1, email);
            int stmtParam = 2;
            if (since != null) stmt.setString(stmtParam++, since);
            if (limit != null) stmt.setInt(stmtParam, new Integer(limit));
            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final PostMainModel post = (PostMainModel) new PostDAOimpl().details(resultSet.getString(1), new ArrayList<String>()).getResponse();
                posts.add(post);
            }

            stmt.close();
            return new ResponseModel(posts, ResponseModel.OK);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }

    @Override
    public ResponseModel updateProfile(String userString) {
        final JsonNode json;
        try {
            json = mapper.readValue(userString, JsonNode.class);
        } catch (IOException e) {
            return new ResponseModel("INVALID REQUEST", ResponseModel.INVALID_REQUEST);
        }

        if (!json.has("user") || !json.has("name") || !json.has("about"))
            return new ResponseModel("INCORRECT REQUEST", ResponseModel.INCORRECT_REQUEST);

        final String email = json.get("user").getTextValue();
        final String name = json.get("name").getTextValue();
        final String about = json.get("about").getTextValue();
        try (Connection connection = Application.connection.getConnection()) {
            final String query = "UPDATE user SET name=?, about=? WHERE email=?";
            final PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, about);
            stmt.setString(3, email);
            stmt.executeUpdate();
            stmt.close();

            return details(email);
        } catch (SQLException e) {
            return new ResponseModel("UNKNOWN ERROR", ResponseModel.UNKNOWN_ERROR);
        }
    }


    public void setFollowers(Connection connection, UserMainModel user) throws SQLException {
        final ArrayList<String> followers = new ArrayList<>();

        final String query = "SELECT follower FROM follows WHERE followed = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            followers.add(resultSet.getString("follower"));
        }
        stmt.close();
        user.setFollowers(followers);
    }

    public void setFollowing(Connection connection, UserMainModel user) throws SQLException {
        final ArrayList<String> following = new ArrayList<>();

        final String query = "SELECT followed FROM follows WHERE follower = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            following.add(resultSet.getString("followed"));
        }
        stmt.close();
        user.setFollowing(following);
    }

    public void setSubscriptions(Connection connection, UserMainModel user) throws SQLException {
        final ArrayList<Integer> subscriptions = new ArrayList<>();

        final String query = "SELECT thread FROM subscribed WHERE user = ?;";
        final PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, user.getEmail());
        final ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            subscriptions.add(resultSet.getInt("thread"));
        }
        stmt.close();
        user.setSubscriptions(subscriptions);
    }
}
