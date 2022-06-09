package pt.ulisboa.tecnico.cmov.cmovproject;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URISyntaxException;
import io.socket.client.Socket;
import io.socket.client.IO;

public class SocketIOApp {

    private static SocketIOApp socketManager;
    private  Socket socketio;

    public SocketIOApp(){
            try {
                this.socketio = IO.socket("http://192.168.1.76:7000/");
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            socketio.connect();
    }

    public static SocketIOApp getInstance(){
        if(socketManager == null){
            socketManager = new SocketIOApp();
        }
            return socketManager;
    }

    public Socket getSocket() {
        return socketio;
    }
}