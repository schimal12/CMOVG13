package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.socket.client.Socket;

public class ChatRoomPicker extends AppCompatActivity implements View.OnClickListener {
    private String username;
    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_picker);

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


        Button createChatRoom = (Button) findViewById(R.id.enterchatroom);
        createChatRoom.setOnClickListener(this);
        Button checkChatRoom = (Button) findViewById(R.id.checkchatroom);
        checkChatRoom.setOnClickListener(this);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(ChatRoomPicker.this, ChatRoom.class);
        toChatRoom.putExtra("username", username);


        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        Log.d("Activity", String.valueOf(appLinkData));
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
                    Intent toChatRoom = new Intent(ChatRoomPicker.this, ChatRoom.class);
                    configEditor = prefs.edit();
                    configEditor.putString("chatroomname",chatRoomName.getText().toString());
                    configEditor.commit();
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