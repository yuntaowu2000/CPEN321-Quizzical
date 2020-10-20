package com.cpen321.quizzical;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Firebase", "Message received in app");
        Handler h = new Handler(Looper.getMainLooper());
        h.post(()->Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show());
    }
}
