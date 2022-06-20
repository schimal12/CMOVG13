package pt.ulisboa.tecnico.cmov.cmovg13.model;

import java.util.Date;

public class Message {
    private String message;
    private String username;


    public Message(String message, String username, Date date) {
        this.message = message;
        this.username = username;
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
}
