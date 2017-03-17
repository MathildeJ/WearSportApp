package com.example.mathilde.wearsportapp;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class StravaLoginService extends WearableListenerService {
    private static final String STRAVA_LOGIN = "/strava_login";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(STRAVA_LOGIN.equals(messageEvent.getPath())){
            Intent dialogIntent = new Intent(this, StravaLoginPageActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
        }
    }
}
