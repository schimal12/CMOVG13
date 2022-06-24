package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;

public class ChatRoom extends AppCompatActivity  {

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
    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;
    public TextView RoomName;

    // --------- camera image ---------
    ImageButton camera_open_id;
    private ImageView mPhotoImageView;                // to show the photo
    private String photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // --------- google maps ---------
    ImageButton maps_button;
    private GoogleMap mMap;
    Boolean actual_ubi_check=false,input_ubi_check=false,marker_ubi_check = false; // from MapsActivity
    Double Actual_ubi_lat,Actual_ubi_long;            // from MapsActivity
    Double Input_ubi_lat,Input_ubi_long;              // from MapsActivity
    Double Marker_ubi_lat, Marker_ubi_long;           // from MapsActivity
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        //Notifying that the application was created.

        Context ctx = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        configEditor = prefs.edit();
        configEditor.putBoolean("activityStarted",true);
        configEditor.commit();

        //Obtaining information about the service
        Boolean status = prefs.getBoolean("serviceStopped",false);
        Log.d("Notification","Status: "+status);
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

        //Getting information from Shared Preferences.

        username = prefs.getString("username","Anonymous");
        roomname = prefs.getString("chatroomname","Anonymous");

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

        //This part needs to be integrated with the messages.

        // to show the photo on the chat
        mPhotoImageView = findViewById(R.id.imageView);

        // --------- Map Activity ---------
        maps_button = (ImageButton) findViewById(R.id.map);
        maps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps_intent = new Intent(ChatRoom.this,MapsActivity.class);
                maps_intent.putExtra("username",username);
                startActivityForResult(maps_intent, 1);
            }
        });

        // --------- Map Fragment no visible in the beginning ---------
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ubi);
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
        mSocket.on("messageMap", ListenUpdateMap);

        //Modification of emit to join to a specific Room.
        mSocket.emit("join", username, roomname);

        //Fill the group with previous messages.so
        mSocket.emit("verifyOldMessages", roomname);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSocket.emit("messageemit", username, message.getText().toString(), roomname,"text");
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
                        Toast.makeText(ChatRoom.this, message, Toast.LENGTH_SHORT).show();
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public Emitter.Listener ListenUpdateMap = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    try {
                        String uNickname = data.getString("uNickname");
                        String messageText = data.getString("message");
                        String [] values = (messageText.split("-"));
                        String lat = values[0];
                        String longM = values[1];
                        Date date = Calendar.getInstance().getTime();

                        LatLng input = new LatLng(Double.parseDouble(lat),Double.parseDouble(longM));
                        Message message = new Message(input,2);
                        ListaMensajes.add(message);
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


    private void openCamera() throws IOException {
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
    // save the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent fromData) {
        super.onActivityResult(requestCode, resultCode, fromData);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
                    Bitmap imgBitMap = BitmapFactory.decodeFile(photoURI);
                    Log.e("hhhhh","imgBitMap");
                    mPhotoImageView.setImageBitmap(imgBitMap);
                }
                if (requestCode == 1) {
                    if(resultCode == Activity.RESULT_OK){
                        double actual_ubi_lat = fromData.getExtras().getDouble("actual_ubi_lat");
                        Actual_ubi_lat = actual_ubi_lat;
                        double actual_ubi_long = fromData.getExtras().getDouble("actual_ubi_long");
                        Actual_ubi_long = actual_ubi_long;
                        actual_ubi_check = fromData.getExtras().getBoolean("actual_ubi_check");
                        if (actual_ubi_check == true){

                            //Emitting message.
                            mSocket.emit("mapemmit", username, Actual_ubi_lat,Actual_ubi_long, roomname, "map");

                           /* LatLng actual = new LatLng(Actual_ubi_lat,Actual_ubi_long);
                            Message message = new Message(actual, 1);
                            ListaMensajes.add(message);
                            recycleViewAdapater = new RecycleViewAdapater(ListaMensajes); //In the onCreate
                            recycleViewAdapater.notifyDataSetChanged();
                            recyclerView.setAdapter(recycleViewAdapater);*/
                        }
                        double search_ubi_lat = fromData.getExtras().getDouble("search_ubi_lat");
                        Input_ubi_lat = search_ubi_lat;
                        double search_ubi_long = fromData.getExtras().getDouble("search_ubi_long");
                        Input_ubi_long = search_ubi_long;
                        input_ubi_check = fromData.getExtras().getBoolean("input_ubi_check");
                        if (input_ubi_check == true){

                            //Emitting message
                            mSocket.emit("mapemmit", username, Input_ubi_lat,Input_ubi_long, roomname, "map");

                          /*  LatLng input = new LatLng(Input_ubi_lat,Input_ubi_long);
                            Message message = new Message(input,2);
                            ListaMensajes.add(message);
                            recycleViewAdapater = new RecycleViewAdapater(ListaMensajes); //In the onCreate
                            recycleViewAdapater.notifyDataSetChanged();
                            recyclerView.setAdapter(recycleViewAdapater);*/

                        }
                        double marker_ubi_lat = fromData.getExtras().getDouble("marker_ubi_lat");
                        Marker_ubi_lat = marker_ubi_lat;
                        double marker_ubi_long = fromData.getExtras().getDouble("marker_ubi_long");
                        Marker_ubi_long = marker_ubi_long;
                        marker_ubi_check = fromData.getExtras().getBoolean("marker_ubi_check");
                        if (marker_ubi_check == true){
                            //Emitting message
                            mSocket.emit("mapemmit", username, Marker_ubi_lat,Marker_ubi_long, roomname,"map");

                            /*LatLng marker = new LatLng(Marker_ubi_lat,Marker_ubi_long);
                            Message message = new Message(marker,3);
                            ListaMensajes.add(message);
                            recycleViewAdapater = new RecycleViewAdapater(ListaMensajes); //In the onCreate
                            recycleViewAdapater.notifyDataSetChanged();
                            recyclerView.setAdapter(recycleViewAdapater);*/
                        }
                        Log.e("actual_ubi_lat: ", String.valueOf(actual_ubi_lat));
                        Log.e("actual_ubi_long: ", String.valueOf(actual_ubi_long));
                        Log.e("search_ubi_lat: ", String.valueOf(search_ubi_lat));
                        Log.e("search_ubi_long: ", String.valueOf(search_ubi_long));
                        Log.e("marker_ubi_lat: ", String.valueOf(marker_ubi_lat));
                        Log.e("marker_ubi_long: ", String.valueOf(marker_ubi_long));
                    }
                    if (resultCode == Activity.RESULT_CANCELED) {
                        // Write your code if there's no result
                    }
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("activity","Closing App");
        configEditor = prefs.edit();
        configEditor.putBoolean("activityStarted", false);
        configEditor.commit();
        startService(new Intent(getBaseContext(), NotificationService.class));
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
}