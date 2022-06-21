package pt.ulisboa.tecnico.cmov.cmovg13;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

public class seeTheGeoFence extends AppCompatActivity{

    List<Geofence> geofenceList;
    private double latitude = 43;
    private double longitude = 67;
    private float radio = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_see_the_geofence);

        geofenceList.add(new Geofence.Builder()
                .setRequestId("my geofence")
                .setCircularRegion(latitude,longitude,radio)
                .setExpirationDuration(6*60)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }
}