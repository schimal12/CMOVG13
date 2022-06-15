package pt.ulisboa.tecnico.cmov.cmovproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;

public class GeoFenceChatRoom extends AppCompatActivity implements LocationListener {


    //TODO Tim: implement method to check location of user if still same as location of chatroom
    //TODO Tim: test

    private String username;
    private String roomname;
    private Location chatroomLocation;
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

    private Boolean sameLocation;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        RoomName = (TextView) findViewById(R.id.RoomID);
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
        chatroomLocation = (Location) fromUsername.getExtras().get("chatroomLocation");
        RoomName.setText("Room: " + roomname);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 3, this);


        // CAMERA
        // by ID we can get each component which id is assigned in XML file
        // get Buttons and ImageView
        camera_open_id = (ImageButton) findViewById(R.id.camera);
        //click_image_id = (ImageView) findViewById(R.id.imageView);

        // camera_open is for open the camera and add the setOnCLickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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


    @Override
    public void onLocationChanged(Location location) {
        //TODO Tim: test and granularity
        sameLocation = location == chatroomLocation;

        if (!sameLocation) {
            mSocket.disconnect();
        }

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
                        JSONArray data0 = (JSONArray) args[0];
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public Emitter.Listener ListenConnection = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String uNickname = data.getString("message");
                        String message = "The user " + uNickname + " has connected";
                        Toast.makeText(GeoFenceChatRoom.this, message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    public Emitter.Listener ListenMessages = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String uNickname = data.getString("uNickname");
                        String messageText = data.getString("message");
                        Date date = Calendar.getInstance().getTime();
                        Message message = new Message(messageText, uNickname, date);
                        ListaMensajes.add(message);
                        for (int i = 0; i < ListaMensajes.size(); i++) {
                            Log.d("Messages", ListaMensajes.get(i).getUsername() + " " + ListaMensajes.get(i).getMessage());
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

    public Emitter.Listener ListenUserRoom = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String) args[0];
                    Toast.makeText(GeoFenceChatRoom.this, data, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    public Emitter.Listener ListerUserDisconnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String) args[0];
                    Toast.makeText(GeoFenceChatRoom.this, data, Toast.LENGTH_SHORT).show();
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
                    for (Object obj : args) {
                        Log.v("Socket Test", "" + obj);
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

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }*/
}
