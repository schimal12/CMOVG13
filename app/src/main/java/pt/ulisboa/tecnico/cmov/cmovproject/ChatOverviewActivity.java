package pt.ulisboa.tecnico.cmov.cmovproject;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatOverviewActivity extends AppCompatActivity {

    ListView chatsListView;
    public SocketIOApp app;
    public Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);

        chatsListView = findViewById(R.id.chats_list_view);


        app = SocketIOApp.getInstance();
        mSocket = app.getSocket();

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError1);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        //Asking the server in which rooms the socket is.
        mSocket.emit("listrooms");
        mSocket.on("thisareyourrooms", ListenerRooms);

        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            String username = "dummy";

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ChatOverviewActivity.this,
                        chatsArray.get(position),
                        Toast.LENGTH_SHORT).show();


                //TODO: pass relevant chatroom info
                Intent intent = new Intent(ChatOverviewActivity.this,
                        ChatRoom.class);
                intent.putExtra("username", username);

                startActivity(intent);

            }
        });
    }




public Emitter.Listener ListenerRooms = new Emitter.Listener(){

    @Override
    public void call(Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = (String)args[0];
                String dataTransformed = data.substring(1, data.length()-1);
                String dataTransformedQuotes = dataTransformed.replace("\"","");
                String[] RoomList = dataTransformedQuotes.split(",");
                for(int j=0; j<RoomList.length;j++){
                    Log.d("Rooms",RoomList[j]);
                }
                //Populate list of Rooms with the Server response.
                ArrayList<String> chatsArray = new ArrayList<>();


                ArrayAdapter<String> chatsArrayAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        chatsArray);

                chatsListView.setAdapter(chatsArrayAdapter);

            }
        });
    }
};
    public Emitter.Listener ListenUserRoom = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String)args[0];
                    Toast.makeText(ChatOverviewActivity.this, data, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public Emitter.Listener ListerUserDisconnected = new Emitter.Listener(){
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String)args[0];
                    Toast.makeText(ChatOverviewActivity.this, data, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("Socket", "Socket Connected!");
        }
    };

    private Emitter.Listener onConnectError1 = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(Object obj:args){
                        Log.v("Socket Test", ""+obj);
                    }
                    Toast.makeText(getApplicationContext(), "Unable to connect to NodeJS server", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }
}