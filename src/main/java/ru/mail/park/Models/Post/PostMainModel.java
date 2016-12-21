package ru.mail.park.Models.Post;

import org.codehaus.jackson.JsonNode;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostMainModel {
    private int id;

    private String date;

    private String message;

    private Integer parent = null;

    private int likes = 0;

    private int dislikes = 0;

    private int points = 0;

    private boolean isApproved = false;

    private boolean isDeleted = false;

    private boolean isEdited = false;

    private boolean isHighlighted = false;

    private boolean isSpam = false;

    private Object forum;

    private Object thread;

    private Object user;

    private int firstPath;

    private String lastPath;

    public PostMainModel(JsonNode json) {
        this.date = json.get("date").getTextValue();
        this.thread = json.get("thread").getIntValue();
        this.forum = json.get("forum").getTextValue();
        this.message = json.get("message").getTextValue();
        this.user = json.get("user").getTextValue();
        if (json.has("isApproved")) { this.isApproved = json.get("isApproved").getBooleanValue(); }
        if (json.has("isHighlighted")) { this.isHighlighted = json.get("isHighlighted").getBooleanValue(); }
        if (json.has("isEdited")) { this.isEdited = json.get("isEdited").getBooleanValue(); }
        if (json.has("isSpam")) { this.isSpam = json.get("isSpam").getBooleanValue(); }
        if (json.has("isDeleted")) { this.isDeleted = json.get("isDeleted").getBooleanValue(); }
        if (json.has("parent")) { this.parent = json.get("parent").getIntValue(); }
    }

    public PostMainModel(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        final String dateWithMs = resultSet.getString("date");
        this.date = dateWithMs.substring(0, dateWithMs.length() - 2);
        this.thread = resultSet.getInt("thread");
        this.forum = resultSet.getObject("forum");
        this.message = resultSet.getString("message");
        this.user = resultSet.getObject("user");
        this.isApproved = resultSet.getBoolean("isApproved");
        this.isHighlighted = resultSet.getBoolean("isHighlighted");
        this.isEdited = resultSet.getBoolean("isEdited");
        this.isSpam = resultSet.getBoolean("isSpam");
        this.isDeleted = resultSet.getBoolean("isDeleted");
        this.parent = (resultSet.getInt("parent") == 0) ? null : resultSet.getInt("parent");
        this.likes = resultSet.getInt("likes");
        this.dislikes = resultSet.getInt("dislikes");
        this.points = resultSet.getInt("points");
        this.firstPath = resultSet.getInt("first_path");
        this.lastPath = resultSet.getString("last_path");
    }

    public int getId() { return id; }

    public void setId( int id ) { this.id = id; }

    public String getDate() {return date; }

    public void setDate( String date ) { this.date = date; }

    public String getMessage() {return message; }

    @SuppressWarnings("unused")
    public void setMessage(String message ) { this.message = message; }

    public Object getForum() {return forum; }

    public void setForum( Object forum ) { this.forum = forum; }

    public Object getThread() {return thread; }

    public void setThread( Object thread ) { this.thread = thread; }

    public Object getUser() {return user; }

    public void setUser( Object user ) { this.user = user; }

    public boolean getIsApproved() {return isApproved; }

    public void setIsApproved( boolean isApproved ) { this.isApproved = isApproved; }

    public boolean getIsHighlighted() {return isHighlighted; }

    public void setIsHighlighted( boolean isHighlighted ) { this.isHighlighted = isHighlighted; }

    public boolean getIsEdited() {return isEdited; }

    public void setIsEdited( boolean isEdited ) { this.isEdited = isEdited; }

    public boolean getIsSpam() {return isSpam; }

    public void setIsSpam( boolean isSpam ) { this.isSpam = isSpam; }

    public boolean getIsDeleted() {return isDeleted; }

    public void setIsDeleted( boolean isDeleted ) { this.isDeleted = isDeleted; }

    public Integer getParent() {return parent; }

    public void setParent( Integer parent ) { this.parent = parent; }

    public int getLikes() {return likes; }

    public void setLikes( int likes ) { this.likes = likes; }

    public int getDislikes() {return dislikes; }

    public void setDislikes( int dislikes ) { this.dislikes = dislikes; }

    public int getPoints() {return points; }

    public void setPoints( int points ) { this.points = points; }

    public int getFirstPathValue() { return firstPath; }

    public String getLastPathValue() { return lastPath; }
}
