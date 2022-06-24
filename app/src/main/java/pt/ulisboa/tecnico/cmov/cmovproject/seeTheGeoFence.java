package pt.ulisboa.tecnico.cmov.cmovproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class seeTheGeoFence extends AppCompatActivity {

    List<Geofence> geofenceList;
    private double latitude = 43;
    private double longitude = 67;
    private float radio = 200;
    private FusedLocationProviderClient fusedLocationClient;
    private Location locationFirst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_see_the_geofence);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent fromData = getIntent();
        double userLat = fromData.getExtras().getDouble("send_latitude");
        double userLong = fromData.getExtras().getDouble("send_longitude");
        float userRadius = fromData.getExtras().getFloat("send_radio");
        Log.e("LAT", String.valueOf(userLat));
        Log.e("LONG", String.valueOf(userLong));
        Log.e("RADIO", String.valueOf(userRadius));

        Location locationFirst = new Location("");
        locationFirst.setLatitude(userLat);
        locationFirst.setLongitude(userLong);

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

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                float[] dist = new float[2];
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Location.distanceBetween(locationFirst.getLatitude(),locationFirst.getLongitude(),location.getLatitude(),location.getLongitude(),dist);
                    Log.e("Distance: ", String.valueOf(dist));
                        if(dist[0]/1000 > userRadius){
                            Intent goBack = new Intent(seeTheGeoFence.this,GeoFenceRooms.class);
                            startActivity(goBack);
                        }
                }
            }
        });

    }
}