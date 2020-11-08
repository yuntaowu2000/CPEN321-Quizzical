package com.cpen321.quizzical;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.cpen321.quizzical.utils.OtherUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SharedPreferences sp = getSharedPreferences(getString(R.string.curr_login_user), MODE_PRIVATE);
        sp.edit().putString(getString(R.string.FIREBASE_TOKEN), token).apply();
        new Thread(() -> OtherUtils.uploadToServer(
                getString(R.string.NOTIFICATION_END_POINT),
                sp.getString(getString(R.string.UID), ""),
                getString(R.string.FIREBASE_TOKEN),
                token
                )).start();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Firebase", "Message received in app");
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> Toast.makeText(getApplicationContext(), remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show());
    }
}
