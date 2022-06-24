package pt.ulisboa.tecnico.cmov.cmovproject.model;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Message {
    private String message;
    private String username;
    private Date date;
    private int TypeMessage;


    private LatLng position;

    public Message(String message, String username, Date date) {
        this.message = ": "+message;
        this.username = username;
        this.date = date;
        this.TypeMessage = 1;
    }

    public Message(LatLng position, int typeMap){
        this.position = position;
        this.TypeMessage = 2;
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

    public LatLng getPosition() {
        return position;
    }

    public int getTypeMessage() {
        return TypeMessage;
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
