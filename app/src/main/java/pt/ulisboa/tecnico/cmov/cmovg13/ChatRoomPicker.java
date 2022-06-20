package pt.ulisboa.tecnico.cmov.cmovg13;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import io.socket.client.Socket;

public class ChatRoomPicker extends AppCompatActivity implements View.OnClickListener {
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_picker);

        Button createChatRoom = (Button) findViewById(R.id.enterchatroom);
        createChatRoom.setOnClickListener(this);
        Button checkChatRoom = (Button) findViewById(R.id.checkchatroom);
        checkChatRoom.setOnClickListener(this);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(ChatRoomPicker.this, ChatRoom.class);
        toChatRoom.putExtra("username", username);

    }

    @Override
    public void onClick(View v) {
        EditText chatRoomName = (EditText) findViewById(R.id.chatroomname);

        switch (v.getId()) {
            case R.id.enterchatroom:
                if (chatRoomName.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please, enter a chat room name";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                } else {
                    //Need to implement correct authentication. Could be using NodeJS or Firebase.
                    Intent toChatRoom = new Intent(ChatRoomPicker.this, ChatRoom.class);
                    toChatRoom.putExtra("username", username);
                    toChatRoom.putExtra("chatroomname", chatRoomName.getText().toString());
                    startActivity(toChatRoom);
                }
                break;
            case R.id.checkchatroom:
                Intent toChatRoomOverview = new Intent(ChatRoomPicker.this, ChatOverviewActivity.class);
                toChatRoomOverview.putExtra("username",username);
                startActivity(toChatRoomOverview);
                break;
        }
    }
}