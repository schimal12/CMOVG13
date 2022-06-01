package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatOverviewActivity extends AppCompatActivity {

    ListView chatsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_overview);

        chatsListView = findViewById(R.id.chats_list_view);

        ArrayList<String> chatsArray = new ArrayList<>();

        chatsArray.add("chat1");
        chatsArray.add("chat2");
        chatsArray.add("chat3");
        chatsArray.add("chat4");
        chatsArray.add("chat5");
        chatsArray.add("chat6");
        chatsArray.add("chat7");
        chatsArray.add("chat8");
        chatsArray.add("chat9");
        chatsArray.add("chat10");
        chatsArray.add("chat11");
        chatsArray.add("chat12");
        chatsArray.add("chat13");
        chatsArray.add("chat14");
        chatsArray.add("chat15");
        chatsArray.add("chat16");
        chatsArray.add("chat17");
        chatsArray.add("chat18");
        chatsArray.add("chat19");
        chatsArray.add("chat20");

        ArrayAdapter<String> chatsArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                chatsArray);
        chatsListView.setAdapter(chatsArrayAdapter);


        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            String username = "dummy";

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ChatOverviewActivity.this,
                        chatsArray.get(position),
                        Toast.LENGTH_SHORT).show();


                //TODO: pass relevant chatroom info
                Intent intent = new Intent(ChatOverviewActivity.this,
                        ChatRoom.class);
                intent.putExtra("username", username);

                startActivity(intent);

            }
        });
    }
}