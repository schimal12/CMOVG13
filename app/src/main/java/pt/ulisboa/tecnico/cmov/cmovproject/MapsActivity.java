package pt.ulisboa.tecnico.cmov.cmovproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.cmovproject.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    String username;
    private GoogleMap mMap;
    ImageView imageViewSearch;
    EditText inputLocation;

    Button actual_ubi,specific_ubi,marker_ubi;
    // for ChatRoom
    double actual_ubi_lat, actual_ubi_long;
    double search_ubi_lat, search_ubi_long;
    double marker_ubi_lat, marker_ubi_long;
    Boolean actual_ubi_check=false,input_ubi_check=false,marker_ubi_check=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        inputLocation = findViewById(R.id.inputLocation);
        actual_ubi = findViewById(R.id.actual_ubi);
        specific_ubi = findViewById(R.id.specific_ubi);
        marker_ubi = findViewById(R.id.marker_ubi);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocalization();

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = inputLocation.getText().toString();
                if (location == null){
                    Toast.makeText(MapsActivity.this,"Type any location name",Toast.LENGTH_SHORT).show();
                }
                else{
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                    try {
                        List<Address> listAddress = geocoder.getFromLocationName(location,1);
                        if (listAddress.size()>0){
                            LatLng latLng = new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("My search location");
                            markerOptions.position(latLng);
                            mMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,5);
                            mMap.animateCamera(cameraUpdate);
                            // for ChatRoom
                            search_ubi_long = latLng.longitude;
                            search_ubi_lat = latLng.latitude;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        actual_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromUsername = getIntent();
                username = fromUsername.getExtras().getString("username");
                // send to ChatRoom
                Intent chat_intent = new Intent(MapsActivity.this,ChatRoom.class);
                chat_intent.putExtra("username",username);
                chat_intent.putExtra("actual_ubi_lat", actual_ubi_lat);
                chat_intent.putExtra("actual_ubi_long", actual_ubi_long);
                actual_ubi_check = true;
                chat_intent.putExtra("actual_ubi_check", actual_ubi_check);
                setResult(Activity.RESULT_OK,chat_intent);
                finish();
            }
        });

        specific_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromUsername = getIntent();
                username = fromUsername.getExtras().getString("username");
                Intent chat_intent2 = new Intent(MapsActivity.this,ChatRoom.class);
                // send to ChatRoom
                chat_intent2.putExtra("username",username);
                chat_intent2.putExtra("search_ubi_lat", search_ubi_lat);
                chat_intent2.putExtra("search_ubi_long", search_ubi_long);
                input_ubi_check = true;
                chat_intent2.putExtra("input_ubi_check", input_ubi_check);
                setResult(Activity.RESULT_OK,chat_intent2);
                finish();
            }
        });

        marker_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromUsername = getIntent();
                username = fromUsername.getExtras().getString("username");
                Intent chat_intent3 = new Intent(MapsActivity.this,ChatRoom.class);
                // send to ChatRoom
                chat_intent3.putExtra("username",username);
                chat_intent3.putExtra("marker_ubi_lat", marker_ubi_lat);
                chat_intent3.putExtra("marker_ubi_long", marker_ubi_long);
                marker_ubi_check = true;
                chat_intent3.putExtra("marker_ubi_check",marker_ubi_check);
                setResult(Activity.RESULT_OK,chat_intent3);
                finish();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapLongClickListener(this);

        LocationManager locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
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

            @Override
            public void onStatusChanged(String provider,int status, Bundle extras){

            }

            @Override
            public void onProviderEnabled(String provider){

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng));
        marker_ubi_lat = latLng.latitude;
        marker_ubi_long = latLng.longitude;
    }
}