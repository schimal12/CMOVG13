package pt.ulisboa.tecnico.cmov.cmovproject;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.emitter.Emitter;
import io.socket.client.Socket;

public class ChatOverviewActivity extends AppCompatActivity {

    public ListView chatsListView;
    public EditText searchBar;
    public SocketIOApp app;
    public Socket mSocket;
    public List<String> RoomArrayList;
    public String username;
    ArrayAdapter<String> chatsArrayAdapter;
    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);

        //Notifying that the application was created.

        Context ctx = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        configEditor = prefs.edit();
        configEditor.putBoolean("activityStarted",true);
        configEditor.commit();

        //Obtaining information about the service
        Boolean status = prefs.getBoolean("serviceStopped",false);
        if(status){

            Log.d("service","not running");
            Log.d("activity running",prefs.getBoolean("activityStarted",false)+"");

        }
        else{
            Log.d("service running","true");
            Log.d("activity running", prefs.getBoolean("activityStarted", false) + "");
            stopService(new Intent(getBaseContext(), NotificationService.class));
        }

        chatsListView = findViewById(R.id.chats_list_view);
        searchBar = findViewById(R.id.searchBar);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username");

        app = SocketIOApp.getInstance();
        mSocket = app.getSocket();

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError1);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.emit("listrooms");
        mSocket.on("thisareyourrooms", ListenerRooms);

        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String room = (String) chatsListView.getItemAtPosition(position);
                Intent toChatRoom = new Intent(ChatOverviewActivity.this, ChatRoom.class);
                toChatRoom.putExtra("chatroomname", room);
                toChatRoom.putExtra("username", username);
                startActivity(toChatRoom);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { chatsArrayAdapter.getFilter().filter(s);}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }





public Emitter.Listener ListenerRooms = new Emitter.Listener(){

    @Override
    public void call(Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONArray data0 = (JSONArray)args[0];
                String data =  data0.toString();
                String[] RoomList  = data.substring(1, data.length()-1).replace("\"","").split(",");

                for(int j=0; j<RoomList.length;j++){
                    Log.d("Rooms",RoomList[j]);
                }
                //Populate list of Rooms with the Server response.
                RoomArrayList = new ArrayList<String>(Arrays.asList(RoomList));
                RoomArrayList.remove(0);
                chatsArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, RoomArrayList);
                chatsListView.setAdapter(chatsArrayAdapter);
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
    protected void onPause(){
        super.onPause();
        Log.d("activity","Closing App");

        configEditor = prefs.edit();
        configEditor.putBoolean("activityStarted", false);
        configEditor.commit();
        startService(new Intent(getBaseContext(), NotificationService.class));
        mSocket.disconnect();
    }
}