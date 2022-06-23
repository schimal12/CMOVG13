package pt.ulisboa.tecnico.cmov.cmovproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.emitter.Emitter;
import io.socket.client.Socket;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;

public class GeoFenceChatRoom extends AppCompatActivity implements OnMapReadyCallback {

    private Socket mSocket;
    private String username;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;

    public TextView RoomName;
    private String roomname;

    public SocketIOApp app;


    // --------- camera image ---------
    ImageButton camera_open_id;
    private ImageView mPhotoImageView;                // to show the photo
    private String photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // --------- google maps ---------
    ImageButton maps_button;
    private GoogleMap mMap;
    Boolean actual_ubi_check,input_ubi_check = false; // from MapsActivity
    Double Actual_ubi_lat,Actual_ubi_long;            // from MapsActivity
    Double Input_ubi_lat,Input_ubi_long;              // from MapsActivity
    SupportMapFragment mapFragment;

    // --------- From GeoFerenceRooms ---------
    private double receive_longitude;
    private double receive_latitude;
    private String receive_radio;

    // --------- GeoFence ---------
    private GeofencingClient geofencingClient;


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

        RoomName = (TextView)findViewById(R.id.RoomID);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username");
        roomname = fromUsername.getExtras().getString("chatroomname");
        RoomName.setText("Room: "+roomname);

        // --------- Camera ---------
        camera_open_id = (ImageButton) findViewById(R.id.camera);
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(camera_intent);
            }
        });
        // to show the photo on the chat
        mPhotoImageView = findViewById(R.id.imageView);

        // --------- Map Activity ---------
        maps_button = (ImageButton) findViewById(R.id.map);
        maps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps_intent = new Intent(GeoFenceChatRoom.this,MapsActivity.class);
                maps_intent.putExtra("username",username);
                startActivity(maps_intent);
            }
        });

        // --------- Map Fragment no visible in the beginning ---------
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ubi);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.GONE);

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

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Write something, :)";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                } else {
                    mSocket.emit("messagedetection", username, message.getText());
                    message.setText("");
                }
            }
        });
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiveDataFromGeoFence();
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
                        Toast.makeText(GeoFenceChatRoom.this, message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GeoFenceChatRoom.this, data, Toast.LENGTH_SHORT).show();
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


    private void receiveDataFromGeoFence(){
        Intent fromData = getIntent();
        username = fromData.getExtras().getString("username");
        receive_latitude = fromData.getDoubleExtra("send_latitude",0);
        receive_longitude = fromData.getDoubleExtra("send_longitude",0);
        receive_radio = fromData.getExtras().getString("send_radio");
        Log.d("username",username);
        Log.d("RADIO",receive_radio);
        Log.d("longitude", String.valueOf(receive_longitude));
        Log.d("latitude", String.valueOf(receive_latitude));
    }

    private void openCamera() throws  IOException{
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imgFile = null;
        try{
            imgFile = createImageFile();

        }catch (IOException ex){
            Log.e("Error", ex.toString());
        }
        if(imgFile != null)
        {
            Uri imgUri = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", imgFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        }

        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
    }

    // save the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap imgBitMap = BitmapFactory.decodeFile(photoURI);
            mPhotoImageView.setImageBitmap(imgBitMap);
        }
    }

    private File createImageFile() throws  IOException{
        String name = "foto_";
        File folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(name, ".jpg", folder);

        photoURI = image.getAbsolutePath();
        Log.d("PHOTOURI:",photoURI);
        return image;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    // fragment of the map on the chat
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

}
