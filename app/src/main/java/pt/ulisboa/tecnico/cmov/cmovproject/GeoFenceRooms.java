package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeoFenceRooms extends AppCompatActivity implements OnMapReadyCallback {
    public String username;
    public Button confirmLocation;
    private GoogleMap mMap;

    double actual_ubi_lat, actual_ubi_long;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_rooms);


        confirmLocation = (Button) findViewById(R.id.confirmLocation);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(GeoFenceRooms.this, ChatRoom.class);
        toChatRoom.putExtra("username", username);

        confirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Tim: get location name from map
                String chatRoomName = "dummy location";


                Intent toChatRoom = new Intent(GeoFenceRooms.this, ChatRoom.class);
                toChatRoom.putExtra("username", username);
                toChatRoom.putExtra("chatroomname", chatRoomName);
                startActivity(toChatRoom);

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        LocationManager locationManager = (LocationManager) GeoFenceRooms.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Marker marker = mMap.addMarker((new MarkerOptions()
                        .position(myLocation)
                        .title("Current location")
                        .draggable(true)));
                Log.e("MapsActivity marker lat: ", String.valueOf(marker.getPosition().latitude));
                Log.e("MapsActivity marker long: ", String.valueOf(marker.getPosition().longitude));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLocation) // objective
                        .zoom(15)
                        .bearing(90)         // direction
                        .tilt(45)            // change the angle
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // for chatRoom
                actual_ubi_lat = myLocation.latitude;
                actual_ubi_long = myLocation.longitude;
            }


        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }


}