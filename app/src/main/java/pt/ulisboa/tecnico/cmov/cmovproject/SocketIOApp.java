package pt.ulisboa.tecnico.cmov.cmovproject;

import java.net.URISyntaxException;

import io.socket.client.Socket;
import io.socket.client.IO;

public class SocketIOApp {
    private Socket socketio;
    {
        try {
            //IO.Options opts = new IO.Options();
            //opts.transports = new String[] { WebSocket.NAME };
            socketio = IO.socket("http://192.168.1.76:7000/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public Socket getSocket() {
        return socketio;
    }
}