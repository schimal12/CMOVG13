package pt.ulisboa.tecnico.cmov.cmovproject;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    static SharedPreferences.Editor configEditor;
    SharedPreferences prefs;
    private Boolean selected = false;
    boolean[] arr = new boolean[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        Button SignIn = (Button) findViewById(R.id.signInBtn);
        SignIn.setOnClickListener(this);
        Button SignUp = (Button) findViewById(R.id.signUpBtn);
        SignUp.setOnClickListener(this);
        Button AnonymousLog = (Button)findViewById(R.id.anonymousButton);
        AnonymousLog.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.anonymousButton:
                EditText username = (EditText) findViewById(R.id.anonymousLog);
                if(username.getText().toString().isEmpty()){
                    Context context = getApplicationContext();
                    CharSequence text = "You need to enter a username and password";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }else{
                    Intent toChatRoom = new Intent(LoginActivity.this, ChooseTypeofRoom.class);
                    toChatRoom.putExtra("username", username.getText().toString());

                    configEditor = prefs.edit();
                    configEditor.putString("username",username.getText().toString());
                    startActivity(toChatRoom);
                }
                break;
            case R.id.signUpBtn:
                Intent toRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(toRegisterActivity);
                break;
            case R.id.signInBtn:
                EditText username1 = (EditText) findViewById(R.id.signinUsername);
                if(username1.getText().toString().isEmpty()){
                    Context context = getApplicationContext();
                    CharSequence text = "You need to enter a username and password";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toastUserName = Toast.makeText(context, text, duration);
                    toastUserName.show();
                }else{
                    Intent toChatRoom = new Intent(LoginActivity.this, ChooseTypeofRoom.class);
                    toChatRoom.putExtra("username", username1.getText().toString());
                    startActivity(toChatRoom);
                }
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