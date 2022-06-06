package pt.ulisboa.tecnico.cmov.cmovproject;

import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatRoom extends AppCompatActivity {

    //private Socket socket;
    private String username;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;
    public SocketIOApp app;
    public Socket mSocket;
    ImageButton camera_open_id;
    ImageView click_image_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        message = (EditText) findViewById(R.id.message);
        sendMessage = (Button) findViewById(R.id.sendMessage);
        ListaMensajes = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.ChatMessagesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        // CAMERA
        // by ID we can get each component which id is assigned in XML file
        // get Buttons and ImageView
        camera_open_id = (ImageButton) findViewById(R.id.camera);
        click_image_id = (ImageView) findViewById(R.id.imageView);

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

        // MAPS
        // Set the layout content view
        // setContentView(R.layout.activity_chat_room);

        // Get a handle to the fragment and register the callback
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);


        app = new SocketIOApp();
        mSocket = app.getSocket();
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError1);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on("message", ListenMessages);
        mSocket.on("connection", ListenConnection);
        mSocket.on("userjoinedroom", ListenUserRoom);
        mSocket.on("userDisconnected", ListerUserDisconnected);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        mSocket.emit("join", username);


        //Send button.

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("messageemit", username, message.getText().toString());
                message.setText("");
            }
        });
    }

    //Camera functions.


    //Listeners
    public Emitter.Listener ListenConnection = new Emitter.Listener(){

     @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String newUser = (String) args[0];
                    //Showing who is joining to the chat room
                    Toast.makeText(ChatRoom.this, newUser, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public Emitter.Listener ListenMessages = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject)args[0];
            try {
                String uNickname = data.getString("uNickname");
                String messageText = data.getString("message");
                Message message = new Message(messageText, uNickname);
                ListaMensajes.add(message);
                recycleViewAdapater = new RecycleViewAdapater(ListaMensajes);
                recycleViewAdapater.notifyDataSetChanged();
                recyclerView.setAdapter(recycleViewAdapater);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public Emitter.Listener ListenUserRoom = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String)args[0];
                    Toast.makeText(ChatRoom.this, data, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ChatRoom.this, data, Toast.LENGTH_SHORT).show();
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

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(Object obj:args){
                        Log.v("Socket Test", ""+obj);
                    }
                    Log.d("Socket", "Socket Error!");
                }
            });
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

//Ideas for images (Convert them to bytes and read them).
