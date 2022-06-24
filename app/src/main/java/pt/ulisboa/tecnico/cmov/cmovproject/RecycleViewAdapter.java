package pt.ulisboa.tecnico.cmov.cmovproject;


import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import pt.ulisboa.tecnico.cmov.cmovproject.model.Message;

//Examples: https://stackoverflow.com/questions/40584424/simple-android-recyclerview-example
//Examples: https://developer.android.com/guide/topics/ui/layout/recyclerview?hl=es-419

class RecycleViewAdapater extends  RecyclerView.Adapter<RecycleViewAdapater.ViewHolder> {

    private List<Message> ListaMensajes;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView message;
        private SupportMapFragment mapFragment;
        public FrameLayout mapLayout;
        public Message message1;
        Context ctx;


        public ViewHolder(View view) {
            super(view);
            ctx = view.getContext();
            username = (TextView) view.findViewById(R.id.nickname);
            message = (TextView) view.findViewById(R.id.message);
            mapLayout = (FrameLayout) itemView.findViewById(R.id.map);
        }

        public SupportMapFragment getMapFragmentAndCallback(OnMapReadyCallback callback){
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                mapFragment.getMapAsync(callback);
            }

            FragmentManager fragmentManager = ((AppCompatActivity)ctx).getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();

            return mapFragment;
        }

        public void removeMapFragment() {
            if (mapFragment != null) {
                FragmentManager fragmentManager = ((AppCompatActivity)ctx).getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(mapFragment).commitAllowingStateLoss();
                mapFragment = null;
            }
        }
    }

    public RecycleViewAdapater(List<Message> ListaMensajes) {
        this.ListaMensajes = ListaMensajes;
    }

    @Override
    public int getItemCount() {
        return ListaMensajes.size();
    }

    @Override
    public RecycleViewAdapater.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new RecycleViewAdapater.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Need to add date
        Message m = ListaMensajes.get(position);

        if(m.getTypeMessage() == 1){
            Log.d("Mensaje","Es un mensaje normal");
            holder.username.setText(m.getUsername());
            holder.message.setText(m.getMessage());
        }else if(m.getTypeMessage() == 2){
            Log.d("Mensaje","Es un mensaje de Map");
            holder.message1 = m;
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        final Message item = holder.message1;

        if(item != null && item.getTypeMessage() == 2){
            holder.getMapFragmentAndCallback(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    LatLng latLng = item.getPosition();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Current location"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
        }
    }
}

