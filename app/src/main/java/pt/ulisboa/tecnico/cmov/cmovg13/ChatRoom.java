package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatRoom extends AppCompatActivity {

    private Socket socket;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        try {
            //Connecting to the NodeJS server
            socket = IO.socket("http://192.168.1.76:3001/"); // Need to check if the connection works or not.
            socket.connect();
            socket.emit("connection",username);

            //Implementing the listener for socket.io
            //https://socket.io/docs/v4/listening-to-events/
            //https://socket.io/docs/v3/emitting-events/
            socket.on("messagedetection", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String newUser = (String)args[0];
                            //Showing who is joining to the chat room
                            Toast.makeText(ChatRoom.this,newUser,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });


        } catch (URISyntaxException e) {
            Log.e("Error de conexion", String.valueOf(e));
        }

        //I

    }
}