package ru.mail.park.Models.User;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserMainModel {
    private int id;

    private String email;

    private String username;

    private String name;

    private String about;

    private boolean isAnonymous = false;

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private ArrayList<String> followers;

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private ArrayList<String> following;

    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private ArrayList<Integer> subscriptions;

    public UserMainModel(int id, String email, String username, String name, String about,
                         boolean isAnonymous, ArrayList<String> followers,
                         ArrayList<String> following, ArrayList<Integer> subscriptions) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.name = name;
        this.about = about;
        this.isAnonymous = isAnonymous;
        this.followers = followers;
        this.following = following;
        this.subscriptions = subscriptions;
    }

    public UserMainModel(int id, String email, String username, String name, String about,
                         boolean isAnonymous) {
        this(id, email, username, name, about, isAnonymous, new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<Integer>());

    }

    public UserMainModel(ResultSet resultSet) throws SQLException {
        this(
                resultSet.getInt("id"),
                resultSet.getString("email"),
                resultSet.getString("username"),
                resultSet.getString("name"),
                resultSet.getString("about"),
                resultSet.getBoolean("isAnonymous")
        );
    }

    public UserMainModel(JsonNode json) {
        this.email = json.get("email").getTextValue();
        this.username = json.get("username").getTextValue();
        this.name = json.get("name").getTextValue();
        this.about = json.get("about").getTextValue();
        if (json.has("isAnonymous")) { this.isAnonymous = json.get("isAnonymous").getBooleanValue(); }
    }

    public int getId() { return id; }

    public void setId(int id ) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }

    public void setUsername(String username ) { this.username = username; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAbout() { return about; }

    public void setAbout(String about) { this.about = about; }

    public boolean getIsAnonymous() { return isAnonymous; }

    public void setIsAnonymous(boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public ArrayList<String> getFollowers() { return followers; }

    public void setFollowers(ArrayList<String> followers) { this.followers = followers; }

    public ArrayList<String> getFollowing() { return following; }

    public void setFollowing(ArrayList<String> following) { this.following = following; }

    public ArrayList<Integer> getSubscriptions() { return subscriptions; }

    public void setSubscriptions(ArrayList<Integer> subscriptions) { this.subscriptions = subscriptions; }
}
