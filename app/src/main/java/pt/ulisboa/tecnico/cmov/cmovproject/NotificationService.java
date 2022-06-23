package pt.ulisboa.tecnico.cmov.cmovproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class NotificationService extends Service {


    public SocketIOApp app;
    public Socket mSocket;
    public NotificationService esto = this;
    SharedPreferences prefs;
    Context ctx;
    static SharedPreferences.Editor configEditor;

    @Override
    public void onCreate() {
        Log.d("notification","Iniciando el servicio de Notificaciones");
        ctx = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Boolean status = prefs.getBoolean("activityStarted", true);
        if(!status){
            {
                //Notifiy that the service is running.
                configEditor = prefs.edit();
                configEditor.putBoolean("serviceStopped", false);
                configEditor.commit();

                //Listening to Messages
                app = SocketIOApp.getInstance();
                mSocket = app.getSocket();
                mSocket.on("message", ListenMessages); //Implement listener for incoming messages
            }
        }
        else {
            this.onDestroy();
        }
    }

    public void createNotification(String title,String text){
        Log.d("notification","Siandasono");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, "test")
                        .setSmallIcon(R.drawable.ic_search)
                        .setContentTitle(title)
                        .setContentText(text);

        NotificationManager mNotificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d("Notifications","Lugar Raro");
            String channelId = "test";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "CMOVG13",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {

        Log.d("Nnotifications","Hola");
        ctx = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        Boolean status = prefs.getBoolean("activityStarted",true);
        if(!status){
            {
                Log.d("service started",status+"");
            }
        } else {
            this.onDestroy();
        }

        return super.onStartCommand(intent,flags,startId);
    }

    public Emitter.Listener ListenMessages = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject)args[0];
            try {
                String uNickname = data.getString("uNickname");
                String messageText = data.getString("message");
                String rommname = data.getString("roomname");
                Log.d("notification",uNickname);
                Log.d("notification",messageText);
                Log.d("notification",rommname);
                esto.createNotification("New Message","Room :"+rommname+"\n"+uNickname+": "+messageText);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
        @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy(){
        configEditor = prefs.edit();
        configEditor.putBoolean("serviceStopped",true);
        configEditor.commit();
    }

}

