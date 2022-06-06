package pt.ulisboa.tecnico.cmov.cmovproject;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button register = (Button) findViewById(R.id.registerButton);
        register.setOnClickListener(this);
        Button SignIn = (Button) findViewById(R.id.signinButton);
        SignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        EditText username = (EditText) findViewById(R.id.registerUsername);
        EditText password = (EditText) findViewById(R.id.registerPassword);
        EditText email = (EditText) findViewById(R.id.registerEmail);
        switch (v.getId()){
            case R.id.registerButton:
                if(username.getText().toString().isEmpty() || password.getText().toString().isEmpty() || email.getText().toString().isEmpty()){
                    Context context = getApplicationContext();
                    CharSequence text = "You need to enter a username, email and password";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }else{
                    //Need to implement correct authentication. Could be using NodeJS or Firebase.
                    Intent toChatRoom = new Intent(RegisterActivity.this, ChatRoom.class);
                    toChatRoom.putExtra("username", username.getText().toString());
                    startActivity(toChatRoom);
                }
                break;
            case R.id.signinButton:
                Intent toSignIn = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(toSignIn);
                break;
        }
    }
}