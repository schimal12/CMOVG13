package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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

public class ChatRoom extends AppCompatActivity {

    private Socket socket;
    private String username;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;

    // attach a camera image
    // define the button and imageview type variable
    ImageButton camera_open_id; // take the photo
    // show the photo
    private ImageView mPhotoImageView;
    private String photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // google maps
    ImageButton maps_button;

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

        // camera_open is for open the camera and add the setOnCLickListener in this button
        camera_open_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(camera_intent);
            }
        });

        // maps
        maps_button = (ImageButton) findViewById(R.id.map);
        maps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps_intent = new Intent(ChatRoom.this,MapsActivity.class);
                startActivity(maps_intent);
            }
        });

        // to show the photo
        mPhotoImageView = findViewById(R.id.imageView);
        //mPhotoImageView.setOnClickListener((View.OnClickListener) this);

        // MAPS
        // Set the layout content view
        // setContentView(R.layout.activity_chat_room);

        // Get a handle to the fragment and register the callback
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.

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
                    abrirCamara();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void abrirCamara() throws  IOException{
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenArchivo = null;

        try{
            imagenArchivo = createImageFile();

        }catch (IOException ex){
            Log.e("Error", ex.toString());
        }


        if(imagenArchivo != null)
        {
            Uri fotoUri = FileProvider.getUriForFile(this, "com.example.myapplication.fileprovider", imagenArchivo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
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

    /*
    // get a handle to the GoogleMap object and display marker
    @Override
    public void onMapReady(GoogleMap googleMap){
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
    }
     */

    private File createImageFile() throws  IOException{
        String nombreImagen = "foto_";
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreImagen, ".jpg", directorio);

        photoURI = imagen.getAbsolutePath();
        Log.d("PHOTOURI:",photoURI);
        return imagen;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }


}

//Ideas for images (Convert them to bytes and read them).
