package pt.ulisboa.tecnico.cmov.cmovg13;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button SignIn = (Button)findViewById(R.id.signInBtn);
        EditText username = (EditText) findViewById(R.id.signinUsername);
        EditText password = (EditText) findViewById(R.id.signinPassword);

        SignIn.setOnClickListener(new View.OnClickListener()  {

            @Override
            public void onClick(View v) {
                if(username.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    Context context = getApplicationContext();
                    CharSequence text = "You need to enter a username and password";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }else{
                    //Need to implement correct authentication. Could be using NodeJS or Firebase.
                    Intent toChatRoom = new Intent(LoginActivity.this, ChatRoom.class);
                    toChatRoom.putExtra("username", username.getText().toString());
                    startActivity(toChatRoom);
                }
            }
        });
    }
}