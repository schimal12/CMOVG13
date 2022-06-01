package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.cmovg13.model.Message;

public class ChatRoom extends AppCompatActivity {

    private Socket socket;
    private String username;
    public RecyclerView recyclerView;
    public List<Message> ListaMensajes;
    public RecycleViewAdapater recycleViewAdapater;
    public EditText message;
    public Button sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        message = (EditText) findViewById(R.id.message);
        sendMessage = (Button) findViewById(R.id.sendMessage);
        ListaMensajes = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.ChatMessagesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Intent fromUsername = getIntent();
        username = fromUsername.getExtras().getString("username"); // I am not sure of this part, I will check it later.

        try {
            //Connecting to the NodeJS server
            socket = IO.socket("http://194.210.227.178:3001"); //
        } catch (URISyntaxException e) {
            Log.e("Error de conexion", String.valueOf(e));
        }
        Log.d("Useranme", username);
        socket.connect();
        socket.emit("connection", username);

        //Implementing the listeners for socket.io
        //https://socket.io/docs/v4/listening-to-events/
        //https://socket.io/docs/v3/emitting-events/

        socket.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String newUser = (String) args[0];
                        //Showing who is joining to the chat room
                        Toast.makeText(ChatRoom.this, newUser, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String nickname = data.getString("sNickname");
                            String message = data.getString("message");
                            Message m = new Message(nickname, message);
                            ListaMensajes.add(m);
                            recycleViewAdapater = new RecycleViewAdapater(ListaMensajes);
                            recycleViewAdapater.notifyDataSetChanged();
                            recyclerView.setAdapter(recycleViewAdapater);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(ChatRoom.this, data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getText().toString().isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Write something, :)";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                } else {
                    socket.emit("messagedetection", username, message.getText());
                    message.setText("");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

}

//Ideas for images (Convert them to bytes and read them).
