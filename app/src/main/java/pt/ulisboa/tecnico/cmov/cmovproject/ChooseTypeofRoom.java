package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChooseTypeofRoom extends AppCompatActivity {
    private String username;
    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_typeof_room);


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

        Button publicRooms = (Button)findViewById(R.id.enterchatroom);
        Button privateRooms = (Button)findViewById(R.id.privateroom);
        Button geofenceRooms = (Button)findViewById(R.id.geofenceRoom);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username");

        publicRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toPublicChatRoom = new Intent(ChooseTypeofRoom.this, ChatRoomPicker.class);
                toPublicChatRoom.putExtra("username", username);
                startActivity(toPublicChatRoom);
            }
        });

        privateRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toPrivateRoom = new Intent(ChooseTypeofRoom.this, PrivateRoom.class);
                toPrivateRoom.putExtra("username", username);
                startActivity(toPrivateRoom);
            }
        });

        geofenceRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toGeoFenceRoom = new Intent(ChooseTypeofRoom.this, GeoFenceRooms.class);
                toGeoFenceRoom.putExtra("username", username);
                startActivity(toGeoFenceRoom);
            }
        });

    }


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