package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
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

public class GeoFenceRooms extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    //TODO Tim: area and not exact location?
    //TODO Tim: test


    public String username;
    ImageView imageViewSearch;
    EditText inputLocation;
    private GoogleMap googleMap;

    Button actual_ubi, specific_ubi, marker_ubi;
    double actual_ubi_lat, actual_ubi_long;
    double search_ubi_lat, search_ubi_long;
    Boolean actual_ubi_check, input_ubi_check = false;

    //actual_ubi - current location
    //specific_ubi - input location
    //marker_ubi - send pinned location


    public Location chatroomLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_rooms);


        imageViewSearch = findViewById(R.id.imageViewSearch2);
        inputLocation = findViewById(R.id.inputLocation2);

        actual_ubi = findViewById(R.id.actual_ubi2);
        specific_ubi = findViewById(R.id.specific_ubi2);
        marker_ubi = findViewById(R.id.marker_ubi2);


        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(GeoFenceRooms.this, GeoFenceChatRoom.class);
        toChatRoom.putExtra("username", username);

        getLocalizacion();


        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = inputLocation.getText().toString();
                if (location == null) {
                    Toast.makeText(GeoFenceRooms.this, "Type any location name", Toast.LENGTH_SHORT).show();
                } else {
                    Geocoder geocoder = new Geocoder(GeoFenceRooms.this, Locale.getDefault());
                    try {
                        List<Address> listAddress = geocoder.getFromLocationName(location, 1);
                        if (listAddress.size() > 0) {
                            LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("My search location");
                            markerOptions.position(latLng);
                            googleMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
                            googleMap.animateCamera(cameraUpdate);
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
                Intent chat_intent = new Intent(GeoFenceRooms.this, ChatRoom.class);
                // send to ChatRoom
                chat_intent.putExtra("username", username);
                chat_intent.putExtra("actual_ubi_lat", actual_ubi_lat);
                chat_intent.putExtra("actual_ubi_long", actual_ubi_long);
                actual_ubi_check = true;
                chat_intent.putExtra("actual_ubi_check", actual_ubi_check);

                if (chatroomLocation != null) {
                    chat_intent.putExtra("chatroomLocation", chatroomLocation);
                }


                startActivity(chat_intent);
            }
        });

        specific_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromUsername = getIntent();
                username = fromUsername.getExtras().getString("username");
                Intent chat_intent2 = new Intent(GeoFenceRooms.this, GeoFenceChatRoom.class);
                // send to ChatRoom
                chat_intent2.putExtra("username", username);
                chat_intent2.putExtra("search_ubi_lat", search_ubi_lat);
                chat_intent2.putExtra("search_ubi_long", search_ubi_long);
                input_ubi_check = true;
                chat_intent2.putExtra("input_ubi_check", input_ubi_check);

                if (chatroomLocation != null) {
                    chat_intent2.putExtra("chatroomLocation", chatroomLocation);
                }


                startActivity(chat_intent2);
            }
        });

        marker_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fromUsername = getIntent();
                username = fromUsername.getExtras().getString("username");
                Intent chat_intent3 = new Intent(GeoFenceRooms.this, GeoFenceChatRoom.class);
                // send to ChatRoom
                chat_intent3.putExtra("username", username);
                chat_intent3.putExtra("marker_ubi_lat", search_ubi_lat);
                chat_intent3.putExtra("marker_ubi_long", search_ubi_long);

                if (chatroomLocation != null) {
                    chat_intent3.putExtra("chatroomLocation", chatroomLocation);
                }


                startActivity(chat_intent3);
            }
        });

    }

    private void getLocalizacion() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);


        LocationManager locationManager = (LocationManager) GeoFenceRooms.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {


                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Marker marker = GeoFenceRooms.this.googleMap.addMarker((new MarkerOptions()
                        .position(myLocation)
                        .title("Current location")
                        .draggable(true)));
                Log.e("GeoFenceRooms marker lat: ", String.valueOf(marker.getPosition().latitude));
                Log.e("GeoFenceRooms marker long: ", String.valueOf(marker.getPosition().longitude));
                GeoFenceRooms.this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLocation) // objective
                        .zoom(15)
                        .bearing(90)         // direction
                        .tilt(45)            // change the angle
                        .build();
                GeoFenceRooms.this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                // for chatRoom
                actual_ubi_lat = myLocation.latitude;
                actual_ubi_long = myLocation.longitude;

                chatroomLocation = location;

            }


        };
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


//        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(CameraPosition cameraPosition) {
//                checkBounds();
//            }
//        });


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }


//    public void checkBounds() {
//        // allowedbounds must be generated only once, in onCreate, because it will be a fixed area
//        LatLng actualCenter = mMap.getProjection().getVisibleRegion().getCenter();
//        if (allowedBounds.contains(actualCenter)) {
//            return;
//        } else {
//        }
//    }


}