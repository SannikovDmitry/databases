package ru.mail.park.Models.Thread;

import org.codehaus.jackson.JsonNode;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ThreadMainModel {
    private int id;

    private String title;

    private String date;

    private String slug;

    private String message;

    private int likes = 0;

    private int dislikes = 0;

    private int points = 0;

    private int posts = 0;

    private boolean isClosed = false;

    private boolean isDeleted = false;

    private Object forum;

    private Object user;

    public ThreadMainModel(JsonNode json) {
        this.title = json.get("title").getTextValue();
        this.date = json.get("date").getTextValue();
        this.slug = json.get("slug").getTextValue();
        this.message = json.get("message").getTextValue();
        this.isClosed = json.get("isClosed").getBooleanValue();
        this.forum = json.get("forum").getTextValue();
        this.user = json.get("user").getTextValue();
        if (json.has("isDeleted")) { this.isDeleted = json.get("isDeleted").getBooleanValue(); }
    }

    public ThreadMainModel(ResultSet resultSet) throws SQLException{
        this.id = resultSet.getInt("id");
        this.title = resultSet.getString("title");
        final String dateWithMs = resultSet.getString("date");
        this.date = dateWithMs.substring(0, dateWithMs.length() - 2);
        this.slug = resultSet.getString("slug");
        this.message = resultSet.getString("message");
        this.isClosed = resultSet.getBoolean("isClosed");
        this.forum = resultSet.getObject("forum");
        this.user = resultSet.getString("user");
        this.isDeleted = resultSet.getBoolean("isDeleted");
        this.posts = resultSet.getInt("posts");
        this.likes = resultSet.getInt("likes");
        this.dislikes = resultSet.getInt("dislikes");
        this.points = resultSet.getInt("points");
    }

    public int getId() { return id; }

    public void setId( int id ) { this.id = id; }

    public String getTitle() {return title; }

    public void setTitle( String title ) { this.title = title; }

    public String getDate() {return date; }

    public void setDate( String date ) { this.date = date; }

    public String getSlug() {return slug; }

    public void setSlug( String slug ) { this.slug = slug; }

    public String getMessage() {return message; }

    public void setMessage( String message ) { this.message = message; }

    public Object getForum() {return forum; }

    public void setForum( Object forum ) { this.forum = forum; }

    public Object getUser() {return user; }

    public void setUser( Object user ) { this.user = user; }

    public boolean getIsClosed() {return isClosed; }

    public void setIsClosed( boolean isClosed ) { this.isClosed = isClosed; }

    public boolean getIsDeleted() {return isDeleted; }

    public void setIsDeleted( boolean isDeleted ) { this.isDeleted = isDeleted; }

    public int getLikes() {return likes; }

    public void setLikes( int likes ) { this.likes = likes; }

    public int getDislikes() {return dislikes; }

    public void setDislikes( int dislikes ) { this.dislikes = dislikes; }

    public int getPoints() {return points; }

    public void setPoints( int points ) { this.points = points; }

    public int getPosts() {return posts; }

    public void setPosts( int posts ) { this.posts = posts; }
}
