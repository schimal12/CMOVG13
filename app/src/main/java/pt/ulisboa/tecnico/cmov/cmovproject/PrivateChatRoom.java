package pt.ulisboa.tecnico.cmov.cmovproject;

import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PrivateChatRoom extends AppCompatActivity {

    //private Socket socket;
    private String username;
    private String roomname;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;
    public SocketIOApp app;
    public Socket mSocket;
    public TextView RoomName;
    ImageButton camera_open_id;
    ImageView click_image_id;
    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat_room);

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

        RoomName = (TextView)findViewById(R.id.RoomID);
        message = (EditText) findViewById(R.id.message);
        sendMessage = (Button) findViewById(R.id.sendMessage);
        ListaMensajes = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.ChatMessagesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username");
        roomname = fromUsername.getExtras().getString("chatroomname");
        RoomName.setText("Room: "+roomname);

        // CAMERA
        // by ID we can get each component which id is assigned in XML file
        // get Buttons and ImageView
        camera_open_id = (ImageButton) findViewById(R.id.camera);

        // camera_open is for open the camera and add the setOnCLickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(camera_intent);
            }
        });


        app = SocketIOApp.getInstance();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError1);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on("message", ListenMessages);
        mSocket.on("userconnected", ListenConnection);
        mSocket.on("userjoinedroom", ListenUserRoom);
        mSocket.on("userDisconnected", ListerUserDisconnected);
        mSocket.on("updatingMessages", ListenUpdateMessage);

        //Modification of emit to join to a specific Room.
        mSocket.emit("join", username, roomname);

        //Fill the group with previous messages.so
        mSocket.emit("verifyOldMessages", roomname);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("messageemit", username, message.getText().toString(), roomname);
                message.setText(" ");
            }
        });
    }

    //Listeners

    public Emitter.Listener ListenUpdateMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Historial", args[0].toString());
                    try {
                        JSONArray data0 =  (JSONArray)args[0];
                        for (int i = 0; i < data0.length(); i++) {
                            JSONObject Currentmessage = data0.getJSONObject(i);
                            String nickname = Currentmessage.getString("username");
                            String message = Currentmessage.getString("message");
                            Date date = Calendar.getInstance().getTime();
                            Message messageTmp = new Message(message, nickname, date);
                            ListaMensajes.add(messageTmp);
                        }
                        recycleViewAdapater = new RecycleViewAdapater(ListaMensajes); //In the onCreate
                        recycleViewAdapater.notifyDataSetChanged();
                        recyclerView.setAdapter(recycleViewAdapater);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public Emitter.Listener ListenConnection = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    try{
                        String uNickname = data.getString("message");
                        String message = "The user "+uNickname+" has connected";
                        Toast.makeText(PrivateChatRoom.this, message, Toast.LENGTH_SHORT).show();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    public Emitter.Listener ListenMessages = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    try {
                        String uNickname = data.getString("uNickname");
                        String messageText = data.getString("message");
                        Date date = Calendar.getInstance().getTime();
                        Message message = new Message(messageText, uNickname, date);
                        ListaMensajes.add(message);
                        for(int i = 0; i<ListaMensajes.size();i++){
                            Log.d("Messages", ListaMensajes.get(i).getUsername()+" "+ListaMensajes.get(i).getMessage());
                        }
                        recycleViewAdapater = new RecycleViewAdapater(ListaMensajes); //In the onCreate
                        recycleViewAdapater.notifyDataSetChanged();
                        recyclerView.setAdapter(recycleViewAdapater);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                    Toast.makeText(PrivateChatRoom.this, data, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(PrivateChatRoom.this, data, Toast.LENGTH_SHORT).show();
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
    protected void onDestroy(){
        super.onDestroy();
        Log.d("activity","Closing App");
        configEditor = prefs.edit();
        configEditor.putBoolean("activityStarted", false);
        configEditor.commit();
        startService(new Intent(getBaseContext(), NotificationService.class));
    }
}
