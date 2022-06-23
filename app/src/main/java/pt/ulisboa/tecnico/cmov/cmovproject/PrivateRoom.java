package pt.ulisboa.tecnico.cmov.cmovproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class PrivateRoom extends AppCompatActivity {

    public String username;
    public EditText namePrivateRoom;
    public Button goPrivateRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_room);

        namePrivateRoom = (EditText)findViewById(R.id.privatechatroomname);
        goPrivateRoom = (Button)findViewById(R.id.enterprivatechatRoom);

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.
        Intent toChatRoom = new Intent(PrivateRoom.this, ChatRoom.class);
        toChatRoom.putExtra("username", username);

/*
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                        .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                            @Override
                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                Uri deepLink = null;
                                if (pendingDynamicLinkData != null) {
                                    deepLink = pendingDynamicLinkData.getLink();
                                    Log.d("DeepLink: ", String.valueOf(deepLink));
                                }
                            }
                        }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FirebaseDynamicLink", "OnFailure",e);
                    }
                });*/

        goPrivateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (namePrivateRoom.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please, enter a chat room name";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                } else {
                    //Need to implement correct authentication. Could be using NodeJS or Firebase.
                    Intent toChatRoom = new Intent(PrivateRoom.this, ChatRoom.class);
                    toChatRoom.putExtra("username", username);
                    toChatRoom.putExtra("chatroomname", namePrivateRoom.getText().toString());
                    startActivity(toChatRoom);
                }
            }
        });


    }
}