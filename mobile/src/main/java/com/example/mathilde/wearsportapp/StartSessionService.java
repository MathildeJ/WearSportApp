package com.example.mathilde.wearsportapp;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class StartSessionService extends WearableListenerService {
    private static final String START_SESSION_PATH = "/start_session";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(START_SESSION_PATH.equals(messageEvent.getPath())){
            Intent intent = new Intent(this, StartSessionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}