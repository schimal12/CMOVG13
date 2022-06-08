package pt.ulisboa.tecnico.cmov.cmovproject.model;


import java.util.Date;

public class Message {
    private String message;
    private String username;
    private Date date;


    public Message(String message, String username, Date date) {
        this.message = message;
        this.username = username;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
