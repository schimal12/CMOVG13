package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeoFenceRooms extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    double actual_ubi_lat, actual_ubi_long;

    public String username;
    public EditText nameGeoFenceRoom;

    private EditText radio;
    private double send_latitude;
    private double send_longitude;
    private String send_radio;
    public Button actual_loc,input_loc,marker_loc;
    private Boolean actual_enabled,input_enabled,marker_enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_rooms);

        actual_enabled = false;
        input_enabled = false;
        marker_enabled = false;

        nameGeoFenceRoom = (EditText)findViewById(R.id.geoFenceChatRoomName);
        radio = (EditText)findViewById(R.id.radio);

        actual_loc = (Button)findViewById(R.id.actual_ubi);
        input_loc = (Button)findViewById(R.id.input_ubi);
        marker_loc = (Button)findViewById(R.id.marker_ubi);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocalization();

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(GeoFenceRooms.this, ChatRoom.class);
        toChatRoom.putExtra("username", username);


        actual_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actual_enabled = true;
                if (nameGeoFenceRoom.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please, enter a chat room name";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }
                else if (radio.getText().toString().isEmpty()){
                    Context context = getApplicationContext();
                    CharSequence text = "Please, enter a radio";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }
                else if (actual_enabled == false && input_enabled == false && marker_enabled == false){
                    Context context = getApplicationContext();
                    CharSequence text = "Please, choose a location";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }
                else {
                    //Need to implement correct authentication. Could be using NodeJS or Firebase.
                    Intent toGeoFenceChatRoom = new Intent(GeoFenceRooms.this, GeoFenceChatRoom.class);
                    toGeoFenceChatRoom.putExtra("username", username);
                    toGeoFenceChatRoom.putExtra("chatroomname", nameGeoFenceRoom.getText().toString());
                    toGeoFenceChatRoom.putExtra("send_latitude",send_latitude);
                    toGeoFenceChatRoom.putExtra("send_longitude",send_longitude);
                    send_radio = radio.getText().toString();
                    toGeoFenceChatRoom.putExtra("send_radio",send_radio);
                    startActivity(toGeoFenceChatRoom);
                }
            }
        });

        input_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input_enabled = true;
            }
        });

        marker_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker_enabled = true;
            }
        });

    }

    private void getLocalization() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission == PackageManager.PERMISSION_DENIED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }


    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        LocationManager locationManager = (LocationManager) GeoFenceRooms.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
                Marker marker = mMap.addMarker((new MarkerOptions()
                        .position(myLocation)
                        .title("Current location")
                        .draggable(true)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLocation) // objective
                        .zoom(15)
                        .bearing(90)         // direction
                        .tilt(45)            // change the angle
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // for GeoFenceChatRoom
                send_latitude = myLocation.latitude;
                Log.e("Latitude ", String.valueOf(send_latitude));
                send_longitude = myLocation.longitude;
                Log.e("LOngitude ", String.valueOf(send_longitude));
            }

            @Override
            public void onStatusChanged(String provider,int status, Bundle extras){

            }

            @Override
            public void onProviderEnabled(String provider){

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);

    }
}