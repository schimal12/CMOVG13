package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.service.autofill.ImageTransformation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.cmov.cmovg13.model.Message;

public class ChatRoom extends AppCompatActivity implements OnMapReadyCallback {

    private Socket socket;
    private String username;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;

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
                Intent maps_intent = new Intent(ChatRoom.this,MapsActivity.class);
                maps_intent.putExtra("username",username);
                startActivity(maps_intent);
            }
        });

        // --------- Map Fragment no visible in the beginning ---------
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ubi);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.GONE);

        receiveDataFromMapsActivity();

        try {
            //Connecting to the NodeJS server
            socket = IO.socket("http://194.210.227.178:3001"); //
        } catch (URISyntaxException e) {
            Log.e("Error de conexion", String.valueOf(e));
        }
        Log.d("Useranme", username);
        socket.connect();
        socket.emit("connection", username);

        //Implementing the listeners for socket.io
        //https://socket.io/docs/v4/listening-to-events/
        //https://socket.io/docs/v3/emitting-events/

        socket.on("connection", new Emitter.Listener() {
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
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String nickname = data.getString("sNickname");
                            String message = data.getString("message");
                            Message m = new Message(nickname, message);
                            ListaMensajes.add(m);
                            recycleViewAdapater = new RecycleViewAdapater(ListaMensajes);
                            recycleViewAdapater.notifyDataSetChanged();
                            recyclerView.setAdapter(recycleViewAdapater);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(ChatRoom.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
                    socket.emit("messagedetection", username, message.getText());
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
    }

    private void receiveDataFromMapsActivity(){
        Intent fromData = getIntent();
        username = fromData.getExtras().getString("username");
        double actual_ubi_lat = fromData.getExtras().getDouble("actual_ubi_lat");
        Actual_ubi_lat = actual_ubi_lat;
        double actual_ubi_long = fromData.getExtras().getDouble("actual_ubi_long");
        Actual_ubi_long = actual_ubi_long;
        actual_ubi_check = fromData.getExtras().getBoolean("actual_ubi_check");
        if (actual_ubi_check == true){
            mapFragment.getView().setVisibility(View.VISIBLE);
        }
        double search_ubi_lat = fromData.getExtras().getDouble("search_ubi_lat");
        Input_ubi_lat = search_ubi_lat;
        double search_ubi_long = fromData.getExtras().getDouble("search_ubi_long");
        Input_ubi_long = search_ubi_long;
        input_ubi_check = fromData.getExtras().getBoolean("input_ubi_check");
        if (input_ubi_check == true){
            mapFragment.getView().setVisibility(View.VISIBLE);
        }
        double marker_ubi_lat = fromData.getExtras().getDouble("marker_ubi_lat");
        double marker_ubi_long = fromData.getExtras().getDouble("marker_ubi_long");
        Log.e("actual_ubi_lat: ", String.valueOf(actual_ubi_lat));
        Log.e("actual_ubi_long: ", String.valueOf(actual_ubi_long));
        Log.e("search_ubi_lat: ", String.valueOf(search_ubi_lat));
        Log.e("search_ubi_long: ", String.valueOf(search_ubi_long));
        Log.e("marker_ubi_lat: ", String.valueOf(marker_ubi_lat));
        Log.e("marker_ubi_long: ", String.valueOf(marker_ubi_long));
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
        socket.disconnect();
    }

    // fragment of the map on the chat
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(0,0);
        if (actual_ubi_check == true){
            LatLng actual = new LatLng(Actual_ubi_lat,Actual_ubi_long);
            location = actual;
            mMap.addMarker(new MarkerOptions()
                    .position(actual)
                    .title("Current location"));
        }
        else if (input_ubi_check == true){
            LatLng input = new LatLng(Input_ubi_lat,Input_ubi_long);
            location = input;
            mMap.addMarker(new MarkerOptions()
                    .position(input)
                    .title("Search location"));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(15)
                .bearing(90)
                .tilt(45)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}

