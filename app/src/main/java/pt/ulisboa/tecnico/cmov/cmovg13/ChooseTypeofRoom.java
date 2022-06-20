package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseTypeofRoom extends AppCompatActivity {
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_typeof_room);

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
                Intent toChatRoom = new Intent(ChooseTypeofRoom.this, ChatRoomPicker.class);
                toChatRoom.putExtra("username", username);
            }
        });



    }
}

